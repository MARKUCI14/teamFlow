package edu.portikmark.teamflow.workmanagement.project.api;

import java.util.UUID;
import edu.portikmark.teamflow.workmanagement.project.application.ProjectApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectApplicationService projects;

    public ProjectController(ProjectApplicationService projects) {
        this.projects = projects;
    }

    @GetMapping
    public Page<ProjectResponse> list(
            @AuthenticationPrincipal Jwt principal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return projects.list(userId(principal), pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody CreateProjectRequest request,
                                  @AuthenticationPrincipal Jwt principal) {
        return projects.create(request, principal);
    }

    @GetMapping("/{projectId}")
    public ProjectResponse get(@PathVariable UUID projectId, @AuthenticationPrincipal Jwt principal) {
        return projects.get(projectId, userId(principal));
    }

    @PatchMapping("/{projectId}")
    public ProjectResponse update(@PathVariable UUID projectId,
                                  @Valid @RequestBody UpdateProjectRequest request,
                                  @AuthenticationPrincipal Jwt principal) {
        return projects.update(projectId, request, principal);
    }

    private UUID userId(Jwt principal) {
        return UUID.fromString(principal.getSubject());
    }
}
