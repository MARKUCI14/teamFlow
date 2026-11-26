package edu.portikmark.teamflow.workmanagement.project.api;

import java.time.Instant;
import java.util.UUID;
import edu.portikmark.teamflow.workmanagement.project.domain.Project;

public record ProjectResponse(UUID id, String name, String description, UUID createdBy, Instant createdAt) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(),
                project.getCreatedBy(), project.getCreatedAt());
    }
}
