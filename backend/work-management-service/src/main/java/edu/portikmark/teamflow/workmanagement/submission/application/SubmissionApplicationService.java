package edu.portikmark.teamflow.workmanagement.submission.application;

import java.util.List;
import java.util.UUID;
import edu.portikmark.teamflow.workmanagement.issue.api.CreateLinkedIssueRequest;
import edu.portikmark.teamflow.workmanagement.issue.api.IssueResponse;
import edu.portikmark.teamflow.workmanagement.issue.domain.WorkIssue;
import edu.portikmark.teamflow.workmanagement.issue.infrastructure.WorkIssueRepository;
import edu.portikmark.teamflow.workmanagement.membership.domain.ProjectMembership;
import edu.portikmark.teamflow.workmanagement.membership.domain.ProjectRole;
import edu.portikmark.teamflow.workmanagement.membership.infrastructure.ProjectMembershipRepository;
import edu.portikmark.teamflow.workmanagement.project.infrastructure.ProjectRepository;
import edu.portikmark.teamflow.workmanagement.submission.api.CreateSubmissionRequest;
import edu.portikmark.teamflow.workmanagement.submission.api.SubmissionResponse;
import edu.portikmark.teamflow.workmanagement.submission.api.TriageSubmissionRequest;
import edu.portikmark.teamflow.workmanagement.submission.domain.Submission;
import edu.portikmark.teamflow.workmanagement.submission.infrastructure.SubmissionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SubmissionApplicationService {

    private final ProjectRepository projects;
    private final ProjectMembershipRepository memberships;
    private final SubmissionRepository submissions;
    private final WorkIssueRepository issues;
    private final ApplicationEventPublisher events;

    public SubmissionApplicationService(ProjectRepository projects, ProjectMembershipRepository memberships,
                                        SubmissionRepository submissions, WorkIssueRepository issues,
                                        ApplicationEventPublisher events) {
        this.projects = projects;
        this.memberships = memberships;
        this.submissions = submissions;
        this.issues = issues;
        this.events = events;
    }

    @Transactional
    public SubmissionResponse create(UUID projectId, CreateSubmissionRequest request, Jwt principal) {
        UUID userId = userId(principal);
        ProjectMembership membership = requireMembership(projectId, userId);
        if (membership.getRole() != ProjectRole.STAKEHOLDER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project stakeholders can submit requests");
        }
        Submission submission = submissions.save(new Submission(projectId, userId, request.title(),
                request.description(), request.type()));
        events.publishEvent(submission);
        return SubmissionResponse.from(submission);
    }

    @Transactional(readOnly = true)
    public Page<SubmissionResponse> list(UUID projectId, Jwt principal, Pageable pageable) {
        UUID userId = userId(principal);
        ProjectMembership membership = requireMembership(projectId, userId);
        Page<Submission> visible = canTriage(membership, principal)
                ? submissions.findByProjectIdOrderByCreatedAtDesc(projectId, pageable)
                : submissions.findByProjectIdAndAuthorIdOrderByCreatedAtDesc(projectId, userId, pageable);
        return visible.map(SubmissionResponse::from);
    }

    @Transactional(readOnly = true)
    public SubmissionResponse get(UUID submissionId, Jwt principal) {
        Submission submission = findSubmission(submissionId);
        ProjectMembership membership = requireMembership(submission.getProjectId(), userId(principal));
        if (!canTriage(membership, principal) && !submission.getAuthorId().equals(userId(principal))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Submission is not visible to this user");
        }
        return SubmissionResponse.from(submission);
    }

    @Transactional
    public SubmissionResponse triage(UUID submissionId, TriageSubmissionRequest request, Jwt principal) {
        Submission submission = findSubmission(submissionId);
        ProjectMembership membership = requireMembership(submission.getProjectId(), userId(principal));
        requireTriagePermission(membership, principal);
        submission.triage(request.status(), request.clarificationMessage());
        return SubmissionResponse.from(submission);
    }

    @Transactional
    public List<IssueResponse> createIssues(UUID submissionId, List<CreateLinkedIssueRequest> requests, Jwt principal) {
        Submission submission = findSubmission(submissionId);
        ProjectMembership membership = requireMembership(submission.getProjectId(), userId(principal));
        requireTriagePermission(membership, principal);
        for (CreateLinkedIssueRequest request : requests) {
            if (request.assigneeId() != null && !memberships.existsByProjectIdAndUserIdAndRole(
                    submission.getProjectId(), request.assigneeId(), ProjectRole.DEVELOPER)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Issue assignee must be a developer in the project");
            }
        }
        List<WorkIssue> created = requests.stream()
                .map(request -> issues.save(new WorkIssue(submission.getProjectId(), submission.getId(),
                        userId(principal), request.assigneeId(), request.title(), request.description())))
                .toList();
        return created.stream().map(IssueResponse::from).toList();
    }

    private Submission findSubmission(UUID submissionId) {
        return submissions.findById(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));
    }

    private ProjectMembership requireMembership(UUID projectId, UUID userId) {
        if (!projects.existsById(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }
        return memberships.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private boolean canTriage(ProjectMembership membership, Jwt principal) {
        return membership.getRole() == ProjectRole.PROJECT_MANAGER || isAdmin(principal);
    }

    private void requireTriagePermission(ProjectMembership membership, Jwt principal) {
        if (!canTriage(membership, principal)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only a project manager or project member admin can triage submissions");
        }
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
