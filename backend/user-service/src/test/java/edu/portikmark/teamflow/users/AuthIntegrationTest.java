package edu.portikmark.teamflow.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import edu.portikmark.teamflow.users.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTest {

    private static final String ISSUER = "http://user-service-test";
    private static final Path PRIVATE_KEY;
    private static final Path PUBLIC_KEY;
    @LocalServerPort private int port;
    @Autowired private UserRepository users;

    static {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();
            Path dir = Files.createTempDirectory("teamflow-auth-test");
            PRIVATE_KEY = dir.resolve("private.pem");
            PUBLIC_KEY = dir.resolve("public.pem");
            Files.writeString(PRIVATE_KEY, pem("PRIVATE KEY", pair.getPrivate().getEncoded()));
            Files.writeString(PUBLIC_KEY, pem("PUBLIC KEY", pair.getPublic().getEncoded()));
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:users;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("teamflow.security.issuer", () -> ISSUER);
        registry.add("teamflow.security.private-key", PRIVATE_KEY::toString);
        registry.add("teamflow.security.public-key", PUBLIC_KEY::toString);
    }

    @Test
    void registrationReturnsCreated() throws IOException, InterruptedException {
        HttpResponse<String> registration = post("/api/auth/register",
                "student@example.com", "correct-horse-battery");
        assertEquals(201, registration.statusCode(), "Registration should create a user");
    }

    @Test
    void registrationStoresHashedPassword() throws IOException, InterruptedException {
        post("/api/auth/register", "password@example.com", "correct-horse-battery");
        String hash = users.findByEmail("password@example.com").orElseThrow().getPasswordHash();
        assertNotEquals("correct-horse-battery", hash, "The stored password must be hashed");
    }

    @Test
    void duplicateRegistrationReturnsConflict() throws IOException, InterruptedException {
        String email = "duplicate@example.com";
        post("/api/auth/register", email, "correct-horse-battery");
        HttpResponse<String> duplicate = post("/api/auth/register", email, "correct-horse-battery");
        assertEquals(409, duplicate.statusCode(), "Duplicate registration should return conflict");
    }

    @Test
    void issuedTokenAuthenticatesCurrentUser() throws IOException, InterruptedException {
        HttpResponse<String> registration = post("/api/auth/register",
                "current@example.com", "correct-horse-battery");
        String accessToken = registration.body().replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
        HttpResponse<String> current = get("/api/users/me", accessToken);
        assertTrue(current.body().contains("\"email\":\"current@example.com\""),
                "The signed token should identify the registered user");
    }

    @Test
    void jwksPublishesRsaKey() throws IOException, InterruptedException {
        HttpResponse<String> jwks = get("/.well-known/jwks.json", null);
        assertTrue(jwks.body().contains("\"kty\":\"RSA\""), "JWKS should expose an RSA public key");
    }

    @Test
    void jwksOmitsPrivateKey() throws IOException, InterruptedException {
        HttpResponse<String> jwks = get("/.well-known/jwks.json", null);
        assertFalse(jwks.body().contains("\"d\""), "JWKS must not expose private key material");
    }

    @Test
    void loginRejectsWrongPassword() throws IOException, InterruptedException {
        post("/api/auth/register", "another@example.com", "correct-password");
        HttpResponse<String> login = post("/api/auth/login", "another@example.com", "incorrect-password");
        assertEquals(401, login.statusCode(), "Wrong credentials should be rejected");
    }

    @Test
    void protectedEndpointRejectsTamperedToken() throws IOException, InterruptedException {
        String tamperedToken = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJub3BlIn0.invalid";
        HttpResponse<String> response = get("/api/users/me", tamperedToken);
        assertEquals(401, response.statusCode(), "Invalid signatures must not authenticate");
    }

    private HttpResponse<String> post(String path, String email, String password)
            throws IOException, InterruptedException {
        String json = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        HttpRequest request = HttpRequest.newBuilder(URI.create(url(path)))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpResponse<String> get(String path, String token) throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url(path))).GET();
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String pem(String label, byte[] value) {
        String base64 = Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(value);
        return "-----BEGIN " + label + "-----\n" + base64 + "\n-----END " + label + "-----\n";
    }
}
