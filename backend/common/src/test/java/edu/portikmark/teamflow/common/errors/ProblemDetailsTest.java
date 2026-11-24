package edu.portikmark.teamflow.common.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProblemDetailsTest {

    @Test
    void createsProblemDetailWithSharedFields() {
        URI type = URI.create("https://teamflow.example/problems/not-found");
        URI instance = URI.create("/api/projects/123");

        var problem = ProblemDetails.create(404, type, "Not Found", "Project was not found", instance);

        List<Object> expected = List.of(404, type, "Not Found", "Project was not found", instance);
        List<Object> actual =
                List.of(
                        problem.getStatus(),
                        problem.getType(),
                        problem.getTitle(),
                        problem.getDetail(),
                        problem.getInstance());

        assertEquals(expected, actual, "Problem detail should preserve the shared response fields");
    }
}
