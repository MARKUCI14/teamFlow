package edu.portikmark.teamflow.workmanagement.submission.infrastructure;

import java.util.Optional;
import java.util.UUID;
import edu.portikmark.teamflow.workmanagement.submission.domain.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    Page<Submission> findByProjectIdOrderByCreatedAtDesc(UUID projectId, Pageable pageable);

    Page<Submission> findByProjectIdAndAuthorIdOrderByCreatedAtDesc(UUID projectId, UUID authorId, Pageable pageable);

    Optional<Submission> findByIdAndAuthorId(UUID submissionId, UUID authorId);
}
