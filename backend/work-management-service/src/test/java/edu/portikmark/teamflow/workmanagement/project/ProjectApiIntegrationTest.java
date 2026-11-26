package edu.portikmark.teamflow.workmanagement.project;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.portikmark.teamflow.workmanagement.membership.infrastructure.ProjectMembershipRepository;
import edu.portikmark.teamflow.workmanagement.project.infrastructure.ProjectRepository;
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
    "spring.datasource.url=jdbc:h2:mem:workmanagement;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;"
            + "INIT=CREATE SCHEMA IF NOT EXISTS work_management",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.default_schema=work_management",
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://user-service-test",
    "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9999/.well-known/jwks.json"
})
@AutoConfigureMockMvc
@SuppressWarnings({"PMD.UnitTestShouldIncludeAssert", "PMD.SignatureDeclareThrowsException"})
class ProjectApiIntegrationTest {

    private static final UUID ADMIN_ID = UUID.randomUUID();
    private static final UUID MEMBER_ID = UUID.randomUUID();
    private static final UUID OUTSIDER_ID = UUID.randomUUID();
    private static final Pattern PROJECT_ID_PATTERN = Pattern.compile("\\\"id\\\":\\\"([^\\\"]+)\\\"");

    @Autowired private MockMvc mvc;
    @Autowired private ProjectMembershipRepository memberships;
    @Autowired private ProjectRepository projects;

    @BeforeEach
    void clearData() {
        memberships.deleteAll();
        projects.deleteAll();
    }

    @Test
    void projectApiRequiresAuthentication() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/projects")).andExpect(status().isUnauthorized());
    }

    @Test
    void projectCreatorGetsProjectManagerMembershipAndCanListProject() throws Exception {
        MvcResult created = mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .with(jwt(ADMIN_ID.toString(), "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Roadmap\",\"description\":\"Plan\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Roadmap"))
                .andReturn();
        UUID projectId = projectId(created);

        mvc.perform(MockMvcRequestBuilders.get("/api/projects").with(jwt(ADMIN_ID.toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(projectId.toString()));
    }

    @Test
    void onlyProjectMembersCanReadProjectDetails() throws Exception {
        UUID projectId = createProjectAsAdmin("Private project");

        mvc.perform(MockMvcRequestBuilders.get("/api/projects/{id}", projectId)
                        .with(jwt(OUTSIDER_ID.toString(), "ADMIN")))
                .andExpect(status().isNotFound());

        mvc.perform(MockMvcRequestBuilders.get("/api/projects").with(jwt(OUTSIDER_ID.toString(), "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        addMember(projectId, MEMBER_ID, "DEVELOPER", jwt(ADMIN_ID.toString()));
        mvc.perform(MockMvcRequestBuilders.get("/api/projects/{id}", projectId).with(jwt(MEMBER_ID.toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Private project"));
    }

    @Test
    void existingProjectManagerCanCreateAnotherProject() throws Exception {
        createProjectAsAdmin("First");

        mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .with(jwt(ADMIN_ID.toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Second\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Second"));
    }

    @Test
    void nonAdminWithoutProjectManagerMembershipCannotCreateProject() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .with(jwt(OUTSIDER_ID.toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Not allowed\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void projectManagerCanUpdateAndAddMembersButOtherMembersCannotUpdate() throws Exception {
        UUID projectId = createProjectAsAdmin("Initial name");
        addMember(projectId, MEMBER_ID, "DEVELOPER", jwt(ADMIN_ID.toString()));

        mvc.perform(MockMvcRequestBuilders.patch("/api/projects/{id}", projectId)
                        .with(jwt(MEMBER_ID.toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Changed\"}"))
                .andExpect(status().isForbidden());

        mvc.perform(MockMvcRequestBuilders.patch("/api/projects/{id}", projectId)
                        .with(jwt(ADMIN_ID.toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Changed\",\"description\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Changed"))
                .andExpect(jsonPath("$.description").value("Updated"));
    }

    @Test
    void duplicateMembershipReturnsConflict() throws Exception {
        UUID projectId = createProjectAsAdmin("Memberships");
        addMember(projectId, MEMBER_ID, "STAKEHOLDER", jwt(ADMIN_ID.toString()));

        mvc.perform(MockMvcRequestBuilders.post("/api/projects/{id}/members", projectId)
                        .with(jwt(ADMIN_ID.toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + MEMBER_ID + "\",\"role\":\"STAKEHOLDER\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void globalAdminMustBeAddedBeforeReadingAnExistingProject() throws Exception {
        UUID projectId = createProjectAsAdmin("Admin visibility");
        UUID otherAdmin = UUID.randomUUID();

        mvc.perform(MockMvcRequestBuilders.get("/api/projects/{id}", projectId)
                        .with(jwt(otherAdmin.toString(), "ADMIN")))
                .andExpect(status().isNotFound());

        addMember(projectId, otherAdmin, "STAKEHOLDER", jwt(otherAdmin.toString(), "ADMIN"));
        mvc.perform(MockMvcRequestBuilders.get("/api/projects/{id}", projectId)
                        .with(jwt(otherAdmin.toString(), "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void projectNameIsRequired() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .with(jwt(ADMIN_ID.toString(), "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    private UUID createProjectAsAdmin(String name) throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .with(jwt(ADMIN_ID.toString(), "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return projectId(result);
    }

    private void addMember(UUID projectId, UUID userId, String role, JwtRequestPostProcessor principal)
            throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/projects/{id}/members", projectId)
                        .with(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + userId + "\",\"role\":\"" + role + "\"}"))
                .andExpect(status().isCreated());
    }

    private UUID projectId(MvcResult result) throws UnsupportedEncodingException {
        Matcher matcher = PROJECT_ID_PATTERN.matcher(result.getResponse().getContentAsString());
        if (!matcher.find()) {
            throw new IllegalStateException("Project response does not include an id");
        }
        return UUID.fromString(matcher.group(1));
    }

    private JwtRequestPostProcessor jwt(String subject, String... roles) {
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(token -> token.subject(subject).claim("roles", List.of(roles)));
    }
}
