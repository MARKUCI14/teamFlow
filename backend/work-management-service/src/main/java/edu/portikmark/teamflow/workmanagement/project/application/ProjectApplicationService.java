package edu.portikmark.teamflow.workmanagement.project.application;

import java.util.List;
import java.util.UUID;
import edu.portikmark.teamflow.workmanagement.membership.domain.ProjectMembership;
import edu.portikmark.teamflow.workmanagement.membership.domain.ProjectRole;
import edu.portikmark.teamflow.workmanagement.membership.infrastructure.ProjectMembershipRepository;
import edu.portikmark.teamflow.workmanagement.project.api.CreateProjectRequest;
import edu.portikmark.teamflow.workmanagement.project.api.ProjectResponse;
import edu.portikmark.teamflow.workmanagement.project.api.UpdateProjectRequest;
import edu.portikmark.teamflow.workmanagement.project.domain.Project;
import edu.portikmark.teamflow.workmanagement.project.infrastructure.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjectApplicationService {

    private final ProjectRepository projects;
    private final ProjectMembershipRepository memberships;

    public ProjectApplicationService(ProjectRepository projects, ProjectMembershipRepository memberships) {
        this.projects = projects;
        this.memberships = memberships;
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> list(UUID userId, Pageable pageable) {
        return projects.findAccessibleTo(userId, pageable).map(ProjectResponse::from);
    }

    @Transactional
    public ProjectResponse create(CreateProjectRequest request, Jwt principal) {
        UUID userId = userId(principal);
        if (!isAdmin(principal) && !memberships.existsByUserIdAndRole(userId, ProjectRole.PROJECT_MANAGER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only an admin or an existing project manager can create projects");
        }

        Project project = projects.save(new Project(request.name().trim(), request.description(), userId));
        memberships.save(new ProjectMembership(project.getId(), userId, ProjectRole.PROJECT_MANAGER));
        return ProjectResponse.from(project);
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(UUID projectId, UUID userId) {
        Project project = findProject(projectId);
        requireMembership(projectId, userId);
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse update(UUID projectId, UpdateProjectRequest request, Jwt principal) {
        UUID userId = userId(principal);
        Project project = findProject(projectId);
        ProjectMembership membership = requireMembership(projectId, userId);
        if (!isAdmin(principal) && membership.getRole() != ProjectRole.PROJECT_MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project managers can update projects");
        }
        project.update(request.name() == null ? null : request.name().trim(), request.description());
        return ProjectResponse.from(project);
    }

    private Project findProject(UUID projectId) {
        return projects.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private ProjectMembership requireMembership(UUID projectId, UUID userId) {
        return memberships.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
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
