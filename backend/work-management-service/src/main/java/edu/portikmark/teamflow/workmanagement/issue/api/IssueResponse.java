package edu.portikmark.teamflow.workmanagement.issue.api;

import java.time.Instant;
import java.util.UUID;
import edu.portikmark.teamflow.workmanagement.issue.domain.WorkIssue;

public record IssueResponse(
        UUID id,
        UUID projectId,
        UUID submissionId,
        UUID createdBy,
        UUID assigneeId,
        String title,
        String description,
        Instant createdAt) {

    public static IssueResponse from(WorkIssue issue) {
        return new IssueResponse(issue.getId(), issue.getProjectId(), issue.getSubmissionId(), issue.getCreatedBy(),
                issue.getAssigneeId(), issue.getTitle(), issue.getDescription(), issue.getCreatedAt());
    }
}
