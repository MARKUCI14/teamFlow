package edu.portikmark.teamflow.workmanagement.issue.api;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;

public record CreateLinkedIssueRequest(
        @NotBlank String title,
        @NotBlank String description,
        UUID assigneeId) { }
