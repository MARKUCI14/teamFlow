package edu.portikmark.teamflow.workmanagement.membership.infrastructure;

import java.util.Optional;
import java.util.UUID;
import edu.portikmark.teamflow.workmanagement.membership.domain.ProjectMembership;
import edu.portikmark.teamflow.workmanagement.membership.domain.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMembershipRepository extends JpaRepository<ProjectMembership, UUID> {

    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);

    boolean existsByProjectIdAndUserIdAndRole(UUID projectId, UUID userId, ProjectRole role);

    boolean existsByUserIdAndRole(UUID userId, ProjectRole role);

    Optional<ProjectMembership> findByProjectIdAndUserId(UUID projectId, UUID userId);
}
