package edu.portikmark.teamflow.workmanagement.project.infrastructure;

import java.util.UUID;
import edu.portikmark.teamflow.workmanagement.project.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query("select p from Project p where exists "
            + "(select m.id from ProjectMembership m where m.projectId = p.id and m.userId = :userId)")
    Page<Project> findAccessibleTo(@Param("userId") UUID userId, Pageable pageable);
}
