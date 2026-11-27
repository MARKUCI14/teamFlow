package edu.portikmark.teamflow.workmanagement.issue.domain;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "issues")
public class WorkIssue {

    @Id
    private UUID id;

    @Column(name = "project_id", nullable = false, updatable = false)
    private UUID projectId;

    @Column(name = "submission_id", nullable = false, updatable = false)
    private UUID submissionId;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "assignee_id")
    private UUID assigneeId;

    @Column(nullable = false, columnDefinition = "text")
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected WorkIssue() { }

    public WorkIssue(UUID projectId, UUID submissionId, UUID createdBy, UUID assigneeId,
                     String title, String description) {
        this.id = UUID.randomUUID();
        this.projectId = projectId;
        this.submissionId = submissionId;
        this.createdBy = createdBy;
        this.assigneeId = assigneeId;
        this.title = title.trim();
        this.description = description.trim();
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public UUID getSubmissionId() {
        return submissionId;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
