package edu.portikmark.teamflow.workmanagement.submission.events;

import java.util.UUID;
import edu.portikmark.teamflow.workmanagement.submission.domain.SubmissionStatus;
import edu.portikmark.teamflow.workmanagement.submission.domain.SubmissionType;

public record SubmissionCreated(
        UUID submissionId,
        UUID authorId,
        String title,
        SubmissionType type,
        SubmissionStatus status) { }
