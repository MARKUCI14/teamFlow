package edu.portikmark.teamflow.workmanagement.membership.domain;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_memberships")
public class ProjectMembership {

    @Id
    private UUID id;

    @Column(name = "project_id", nullable = false, updatable = false)
    private UUID projectId;

    // The account belongs to user-service, so this UUID is an external reference without a database FK.
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectRole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ProjectMembership() { }

    public ProjectMembership(UUID projectId, UUID userId, ProjectRole role) {
        this.id = UUID.randomUUID();
        this.projectId = projectId;
        this.userId = userId;
        this.role = role;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public UUID getUserId() {
        return userId;
    }

    public ProjectRole getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
