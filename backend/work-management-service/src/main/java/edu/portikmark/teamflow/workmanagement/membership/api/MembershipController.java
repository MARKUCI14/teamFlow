package edu.portikmark.teamflow.workmanagement.membership.api;

import java.util.UUID;
import edu.portikmark.teamflow.workmanagement.membership.application.MembershipApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class MembershipController {

    private final MembershipApplicationService memberships;

    public MembershipController(MembershipApplicationService memberships) {
        this.memberships = memberships;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectMemberResponse add(@PathVariable UUID projectId,
                                     @Valid @RequestBody AddProjectMemberRequest request,
                                     @AuthenticationPrincipal Jwt principal) {
        return memberships.add(projectId, request, principal);
    }
}
