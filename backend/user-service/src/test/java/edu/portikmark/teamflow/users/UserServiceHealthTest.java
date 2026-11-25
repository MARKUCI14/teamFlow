package edu.portikmark.teamflow.users;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserServiceHealthTest {

    private static final Path PRIVATE_KEY;
    private static final Path PUBLIC_KEY;
    @LocalServerPort private int port;

    static {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();
            Path dir = Files.createTempDirectory("teamflow-health-test");
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
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:health;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("teamflow.security.private-key", PRIVATE_KEY::toString);
        registry.add("teamflow.security.public-key", PUBLIC_KEY::toString);
    }

    @Test
    void healthEndpointReturnsUp() throws IOException, InterruptedException {
        HttpRequest request =
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/actuator/health"))
                        .GET()
                        .build();
        HttpResponse<String> response =
                HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertTrue(
                response.statusCode() == 200 && response.body().contains("\"status\":\"UP\""),
                "Unexpected health response: " + response.statusCode() + " " + response.body());
    }

    private static String pem(String label, byte[] value) {
        String base64 = Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(value);
        return "-----BEGIN " + label + "-----\n" + base64 + "\n-----END " + label + "-----\n";
    }
}
