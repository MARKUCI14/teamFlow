package edu.portikmark.teamflow.users.infrastructure;

import java.util.Optional;
import java.util.UUID;
import edu.portikmark.teamflow.users.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
