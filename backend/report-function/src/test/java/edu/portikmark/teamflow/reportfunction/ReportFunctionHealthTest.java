package edu.portikmark.teamflow.reportfunction;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReportFunctionHealthTest {

    @LocalServerPort private int port;

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
}
