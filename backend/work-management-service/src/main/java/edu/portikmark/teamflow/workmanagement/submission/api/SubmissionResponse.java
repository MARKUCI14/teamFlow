package edu.portikmark.teamflow.workmanagement.submission.api;

import java.time.Instant;
import java.util.UUID;
import edu.portikmark.teamflow.workmanagement.submission.domain.Submission;
import edu.portikmark.teamflow.workmanagement.submission.domain.SubmissionStatus;
import edu.portikmark.teamflow.workmanagement.submission.domain.SubmissionType;

public record SubmissionResponse(
        UUID id,
        UUID projectId,
        UUID authorId,
        String title,
        String description,
        SubmissionType type,
        SubmissionStatus status,
        String clarificationMessage,
        Instant createdAt,
        Instant updatedAt) {

    public static SubmissionResponse from(Submission submission) {
        return new SubmissionResponse(submission.getId(), submission.getProjectId(), submission.getAuthorId(),
                submission.getTitle(), submission.getDescription(), submission.getType(), submission.getStatus(),
                submission.getClarificationMessage(), submission.getCreatedAt(), submission.getUpdatedAt());
    }
}
