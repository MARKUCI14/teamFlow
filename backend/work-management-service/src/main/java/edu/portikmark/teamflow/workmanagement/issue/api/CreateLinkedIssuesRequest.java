package edu.portikmark.teamflow.workmanagement.issue.api;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record CreateLinkedIssuesRequest(@NotEmpty List<@Valid CreateLinkedIssueRequest> issues) { }
