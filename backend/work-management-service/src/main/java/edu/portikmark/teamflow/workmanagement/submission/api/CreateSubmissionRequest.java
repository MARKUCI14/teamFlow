package edu.portikmark.teamflow.workmanagement.submission.api;

import edu.portikmark.teamflow.workmanagement.submission.domain.SubmissionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSubmissionRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull SubmissionType type) { }
