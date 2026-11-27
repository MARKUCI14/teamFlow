package edu.portikmark.teamflow.workmanagement.submission.api;

import edu.portikmark.teamflow.workmanagement.submission.domain.SubmissionStatus;
import jakarta.validation.constraints.NotNull;

public record TriageSubmissionRequest(
        @NotNull SubmissionStatus status,
        String clarificationMessage) { }
