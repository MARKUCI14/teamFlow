package edu.portikmark.teamflow.workmanagement.project.api;

import jakarta.validation.constraints.NotBlank;

public record CreateProjectRequest(@NotBlank String name, String description) { }
