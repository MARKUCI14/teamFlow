package edu.portikmark.teamflow.workmanagement.submission.api;

import java.util.List;
import java.util.UUID;
import edu.portikmark.teamflow.workmanagement.issue.api.CreateLinkedIssuesRequest;
import edu.portikmark.teamflow.workmanagement.issue.api.IssueResponse;
import edu.portikmark.teamflow.workmanagement.submission.application.SubmissionApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubmissionController {

    private final SubmissionApplicationService submissions;

    public SubmissionController(SubmissionApplicationService submissions) {
        this.submissions = submissions;
    }

    @PostMapping("/api/projects/{projectId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse create(@PathVariable UUID projectId,
                                     @Valid @RequestBody CreateSubmissionRequest request,
                                     @AuthenticationPrincipal Jwt principal) {
        return submissions.create(projectId, request, principal);
    }

    @GetMapping("/api/projects/{projectId}/submissions")
    public Page<SubmissionResponse> list(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal Jwt principal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return submissions.list(projectId, principal, pageable);
    }

    @GetMapping("/api/submissions/{submissionId}")
    public SubmissionResponse get(@PathVariable UUID submissionId, @AuthenticationPrincipal Jwt principal) {
        return submissions.get(submissionId, principal);
    }

    @PatchMapping("/api/submissions/{submissionId}")
    public SubmissionResponse triage(@PathVariable UUID submissionId,
                                     @Valid @RequestBody TriageSubmissionRequest request,
                                     @AuthenticationPrincipal Jwt principal) {
        return submissions.triage(submissionId, request, principal);
    }

    @PostMapping("/api/submissions/{submissionId}/issues")
    @ResponseStatus(HttpStatus.CREATED)
    public List<IssueResponse> createIssues(@PathVariable UUID submissionId,
                                           @Valid @RequestBody CreateLinkedIssuesRequest request,
                                           @AuthenticationPrincipal Jwt principal) {
        return submissions.createIssues(submissionId, request.issues(), principal);
    }
}
