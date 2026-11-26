package edu.portikmark.teamflow.workmanagement.membership.api;

import java.time.Instant;
import java.util.UUID;
import edu.portikmark.teamflow.workmanagement.membership.domain.ProjectMembership;
import edu.portikmark.teamflow.workmanagement.membership.domain.ProjectRole;

public record ProjectMemberResponse(UUID id, UUID projectId, UUID userId, ProjectRole role, Instant createdAt) {

    public static ProjectMemberResponse from(ProjectMembership membership) {
        return new ProjectMemberResponse(membership.getId(), membership.getProjectId(), membership.getUserId(),
                membership.getRole(), membership.getCreatedAt());
    }
}
