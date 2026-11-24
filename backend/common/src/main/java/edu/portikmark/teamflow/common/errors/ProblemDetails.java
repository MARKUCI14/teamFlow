package edu.portikmark.teamflow.common.errors;

import java.net.URI;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

/** Creates the common RFC 9457 response shape used by service APIs. */
public final class ProblemDetails {

    private ProblemDetails() {}

    public static ProblemDetail create(
            int status, URI type, String title, String detail, URI instance) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status), detail);
        problem.setType(type);
        problem.setTitle(title);
        problem.setInstance(instance);
        return problem;
    }
}
