package edu.portikmark.teamflow.workmanagement.submission.domain;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    private UUID id;

    @Column(name = "project_id", nullable = false, updatable = false)
    private UUID projectId;

    @Column(name = "author_id", nullable = false, updatable = false)
    private UUID authorId;

    @Column(nullable = false, columnDefinition = "text")
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    @Column(name = "clarification_message", columnDefinition = "text")
    private String clarificationMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Submission() { }

    public Submission(UUID projectId, UUID authorId, String title, String description, SubmissionType type) {
        this.id = UUID.randomUUID();
        this.projectId = projectId;
        this.authorId = authorId;
        this.title = title.trim();
        this.description = description.trim();
        this.type = type;
        this.status = SubmissionStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void triage(SubmissionStatus status, String clarificationMessage) {
        this.status = status;
        this.clarificationMessage = clarificationMessage == null ? null : clarificationMessage.trim();
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public SubmissionType getType() {
        return type;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public String getClarificationMessage() {
        return clarificationMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
