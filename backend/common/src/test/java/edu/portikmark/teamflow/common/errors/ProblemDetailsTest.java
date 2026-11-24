package edu.portikmark.teamflow.common.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ProblemDetailsTest {

    @Test
    void createsProblemDetailWithSharedFields() {
        URI type = URI.create("https://teamflow.example/problems/not-found");
        URI instance = URI.create("/api/projects/123");

        var problem = ProblemDetails.create(404, type, "Not Found", "Project was not found", instance);

        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
        assertEquals(type, problem.getType());
        assertEquals("Not Found", problem.getTitle());
        assertEquals("Project was not found", problem.getDetail());
        assertEquals(instance, problem.getInstance());
    }
}
