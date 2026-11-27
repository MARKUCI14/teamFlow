package edu.portikmark.teamflow.workmanagement.submission;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.portikmark.teamflow.workmanagement.issue.infrastructure.WorkIssueRepository;
import edu.portikmark.teamflow.workmanagement.membership.domain.ProjectMembership;
import edu.portikmark.teamflow.workmanagement.membership.domain.ProjectRole;
import edu.portikmark.teamflow.workmanagement.membership.infrastructure.ProjectMembershipRepository;
import edu.portikmark.teamflow.workmanagement.project.domain.Project;
import edu.portikmark.teamflow.workmanagement.project.infrastructure.ProjectRepository;
import edu.portikmark.teamflow.workmanagement.submission.infrastructure.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:submissions;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;"
            + "INIT=CREATE SCHEMA IF NOT EXISTS work_management",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.default_schema=work_management",
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://user-service-test",
    "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9999/.well-known/jwks.json",
    "teamflow.kafka.enabled=false"
})
@AutoConfigureMockMvc
@SuppressWarnings({"PMD.UnitTestShouldIncludeAssert", "PMD.SignatureDeclareThrowsException"})
class SubmissionApiIntegrationTest {

    private static final UUID ADMIN_ID = UUID.randomUUID();
    private static final UUID STAKEHOLDER_ID = UUID.randomUUID();
    private static final UUID OTHER_STAKEHOLDER_ID = UUID.randomUUID();
    private static final UUID DEVELOPER_ID = UUID.randomUUID();
    private static final UUID OUTSIDER_ID = UUID.randomUUID();
    private static final Pattern ID_PATTERN = Pattern.compile("\\\"id\\\":\\\"([^\\\"]+)\\\"");

    @Autowired private MockMvc mvc;
    @Autowired private ProjectRepository projects;
    @Autowired private ProjectMembershipRepository memberships;
    @Autowired private SubmissionRepository submissions;
    @Autowired private WorkIssueRepository issues;

    @BeforeEach
    void clearData() {
        issues.deleteAll();
        submissions.deleteAll();
        memberships.deleteAll();
        projects.deleteAll();
    }

    @Test
    void stakeholderCanCreateAndOnlySeeOwnSubmissionsWhilePmCanSeeAll() throws Exception {
        UUID projectId = createProject();
        addMember(projectId, STAKEHOLDER_ID, ProjectRole.STAKEHOLDER);
        addMember(projectId, OTHER_STAKEHOLDER_ID, ProjectRole.STAKEHOLDER);
        UUID ownSubmission = createSubmission(projectId, STAKEHOLDER_ID, "My report");
        createSubmission(projectId, OTHER_STAKEHOLDER_ID, "Other report");

        mvc.perform(MockMvcRequestBuilders.get("/api/projects/{id}/submissions", projectId)
                        .with(jwt(STAKEHOLDER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(ownSubmission.toString()));

        mvc.perform(MockMvcRequestBuilders.get("/api/submissions/{id}", ownSubmission)
                        .with(jwt(OTHER_STAKEHOLDER_ID)))
                .andExpect(status().isForbidden());

        mvc.perform(MockMvcRequestBuilders.get("/api/projects/{id}/submissions", projectId)
                        .with(jwt(ADMIN_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void onlyStakeholderCanCreateAndOnlyProjectMemberPmOrAdminCanTriage() throws Exception {
        UUID projectId = createProject();
        addMember(projectId, STAKEHOLDER_ID, ProjectRole.STAKEHOLDER);
        addMember(projectId, DEVELOPER_ID, ProjectRole.DEVELOPER);
        UUID submissionId = createSubmission(projectId, STAKEHOLDER_ID, "Triage me");

        mvc.perform(MockMvcRequestBuilders.post("/api/projects/{id}/submissions", projectId)
                        .with(jwt(DEVELOPER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submissionJson("Not allowed")))
                .andExpect(status().isForbidden());

        mvc.perform(MockMvcRequestBuilders.patch("/api/submissions/{id}", submissionId)
                        .with(jwt(OUTSIDER_ID, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACCEPTED\"}"))
                .andExpect(status().isNotFound());

        mvc.perform(MockMvcRequestBuilders.patch("/api/submissions/{id}", submissionId)
                        .with(jwt(ADMIN_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"NEEDS_CLARIFICATION\","
                                + "\"clarificationMessage\":\"Please add steps to reproduce.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEEDS_CLARIFICATION"))
                .andExpect(jsonPath("$.clarificationMessage").value("Please add steps to reproduce."));
    }

    @Test
    void pmCanCreateMultipleLinkedIssuesAssignedToProjectDevelopers() throws Exception {
        UUID projectId = createProject();
        addMember(projectId, STAKEHOLDER_ID, ProjectRole.STAKEHOLDER);
        addMember(projectId, DEVELOPER_ID, ProjectRole.DEVELOPER);
        UUID submissionId = createSubmission(projectId, STAKEHOLDER_ID, "Split work");

        mvc.perform(MockMvcRequestBuilders.post("/api/submissions/{id}/issues", submissionId)
                        .with(jwt(ADMIN_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"issues\":["
                                + "{\"title\":\"First\",\"description\":\"First part\","
                                + "\"assigneeId\":\"" + DEVELOPER_ID + "\"},"
                                + "{\"title\":\"Second\",\"description\":\"Second part\"}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].submissionId").value(submissionId.toString()))
                .andExpect(jsonPath("$[0].assigneeId").value(DEVELOPER_ID.toString()))
                .andExpect(jsonPath("$[1].assigneeId").doesNotExist());
    }

    private UUID createProject() {
        Project project = projects.save(new Project("Submissions", null, ADMIN_ID));
        memberships.save(new ProjectMembership(project.getId(), ADMIN_ID, ProjectRole.PROJECT_MANAGER));
        return project.getId();
    }

    private void addMember(UUID projectId, UUID userId, ProjectRole role) {
        memberships.save(new ProjectMembership(projectId, userId, role));
    }

    private UUID createSubmission(UUID projectId, UUID authorId, String title) throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/api/projects/{id}/submissions", projectId)
                        .with(jwt(authorId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submissionJson(title)))
                .andExpect(status().isCreated())
                .andReturn();
        Matcher matcher = ID_PATTERN.matcher(result.getResponse().getContentAsString());
        if (!matcher.find()) {
            throw new IllegalStateException("Submission response does not include an id");
        }
        return UUID.fromString(matcher.group(1));
    }

    private String submissionJson(String title) {
        return "{\"title\":\"" + title + "\",\"description\":\"Description\",\"type\":\"BUG\"}";
    }

    private JwtRequestPostProcessor jwt(UUID subject, String... roles) {
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(token -> token.subject(subject.toString()).claim("roles", List.of(roles)));
    }
}
