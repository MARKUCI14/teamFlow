package edu.portikmark.teamflow.workmanagement.membership.application;

import java.util.List;
import java.util.UUID;
import edu.portikmark.teamflow.workmanagement.membership.api.AddProjectMemberRequest;
import edu.portikmark.teamflow.workmanagement.membership.api.ProjectMemberResponse;
import edu.portikmark.teamflow.workmanagement.membership.domain.ProjectMembership;
import edu.portikmark.teamflow.workmanagement.membership.domain.ProjectRole;
import edu.portikmark.teamflow.workmanagement.membership.infrastructure.ProjectMembershipRepository;
import edu.portikmark.teamflow.workmanagement.project.infrastructure.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MembershipApplicationService {

    private final ProjectRepository projects;
    private final ProjectMembershipRepository memberships;

    public MembershipApplicationService(ProjectRepository projects, ProjectMembershipRepository memberships) {
        this.projects = projects;
        this.memberships = memberships;
    }

    @Transactional
    public ProjectMemberResponse add(UUID projectId, AddProjectMemberRequest request, Jwt principal) {
        if (!projects.existsById(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }

        UUID requesterId = userId(principal);
        boolean projectManager = memberships.existsByProjectIdAndUserIdAndRole(
                projectId, requesterId, ProjectRole.PROJECT_MANAGER);
        if (!isAdmin(principal) && !projectManager) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }
        if (memberships.existsByProjectIdAndUserId(projectId, request.userId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a project member");
        }

        ProjectMembership membership = memberships.save(
                new ProjectMembership(projectId, request.userId(), request.role()));
        return ProjectMemberResponse.from(membership);
    }

    private boolean isAdmin(Jwt principal) {
        List<String> roles = principal.getClaimAsStringList("roles");
        return roles != null && roles.contains("ADMIN");
    }

    private UUID userId(Jwt principal) {
        String subject = principal.getSubject();
        if (subject == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token subject must be a user UUID");
        }
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token subject must be a user UUID");
        }
    }
}
