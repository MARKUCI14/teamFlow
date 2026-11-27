package edu.portikmark.teamflow.workmanagement.issue.infrastructure;

import java.util.List;
import java.util.UUID;
import edu.portikmark.teamflow.workmanagement.issue.domain.WorkIssue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkIssueRepository extends JpaRepository<WorkIssue, UUID> {

    List<WorkIssue> findBySubmissionIdOrderByCreatedAtAsc(UUID submissionId);
}
