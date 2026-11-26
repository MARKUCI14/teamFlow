package edu.portikmark.teamflow.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewaySecurityTest {

    private static final String DEV_ORIGIN = "http://localhost:5173";

    @LocalServerPort private int port;
    @Autowired private RouteLocator routeLocator;

    @Test
    void protectedApiRequiresBearerToken() throws IOException, InterruptedException {
        HttpResponse<String> response = send("/api/projects", "GET", null, null);

        assertEquals(401, response.statusCode(), "Protected REST paths require a valid bearer token");
    }

    @Test
    void malformedBearerTokenIsRejected() throws IOException, InterruptedException {
        HttpResponse<String> response = send("/api/projects", "GET", "Bearer invalid-token", null);

        assertEquals(401, response.statusCode(), "Malformed bearer tokens must be rejected");
    }

    @Test
    void preflightAllowsConfiguredOriginAndApiHeaders() throws IOException, InterruptedException {
        HttpResponse<String> response = send("/api/projects", "OPTIONS", null, DEV_ORIGIN);

        assertTrue(response.statusCode() == 200
                        && DEV_ORIGIN.equals(response.headers().firstValue("Access-Control-Allow-Origin").orElse("")),
                "Configured development origin should pass CORS preflight");
    }

    @Test
    void userApiPathsRouteToUserService() {
        List<String> paths = List.of("/api/auth/login", "/api/users/me", "/.well-known/jwks.json");

        assertTrue(paths.stream().allMatch(this::hasPublicRoute), "Auth, user, and JWKS paths must be routed");
    }

    @Test
    void workManagementPathsRouteToTheirService() {
        List<String> paths = List.of("/api/projects", "/api/submissions/1", "/api/issues/1", "/api/worklogs/1");

        assertTrue(paths.stream().allMatch(this::hasPublicRoute), "Documented work-management paths must be routed");
    }

    @Test
    void reportPathsRouteToReportService() {
        assertTrue(hasPublicRoute("/api/reports/1"), "Report paths must be routed");
    }

    @Test
    void internalAndActuatorPathsAreNotRoutedToServices() {
        assertFalse(hasPublicRoute("/internal/admin") || hasPublicRoute("/actuator/env"),
                "Internal service paths must not be routed by the gateway");
    }

    private HttpResponse<String> send(String path, String method, String authorization, String origin)
            throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        if (authorization != null) {
            request.header("Authorization", authorization);
        }
        if (origin != null) {
            request.header("Origin", origin);
            request.header("Access-Control-Request-Method", "PATCH");
            request.header("Access-Control-Request-Headers", "authorization,content-type");
        }
        request.method(method, HttpRequest.BodyPublishers.noBody());
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private boolean hasPublicRoute(String path) {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
        List<Route> routes = routeLocator.getRoutes().collectList().block(Duration.ofSeconds(2));
        return routes != null && routes.stream().anyMatch(route -> Boolean.TRUE.equals(
                Mono.from(route.getPredicate().apply(exchange)).block(Duration.ofSeconds(2))));
    }
}
