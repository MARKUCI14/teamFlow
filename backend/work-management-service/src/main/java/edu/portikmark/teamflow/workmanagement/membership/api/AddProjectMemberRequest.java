package edu.portikmark.teamflow.workmanagement.membership.api;

import java.util.UUID;
import edu.portikmark.teamflow.workmanagement.membership.domain.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record AddProjectMemberRequest(@NotNull UUID userId, @NotNull ProjectRole role) { }
