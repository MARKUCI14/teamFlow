package edu.portikmark.teamflow.workmanagement.project.api;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;

public record UpdateProjectRequest(
        @Pattern(regexp = ".*\\S.*", message = "Project name must not be blank") String name,
        String description) {

    @AssertTrue(message = "Provide at least one project field to update")
    public boolean hasChanges() {
        return name != null || description != null;
    }
}
