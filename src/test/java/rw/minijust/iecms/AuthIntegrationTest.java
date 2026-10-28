package rw.minijust.iecms;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import rw.minijust.iecms.security.DemoUserSeeder;

/** End to end: real login, real JWT, real role checks, lockout and audit trail. */
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {
    static final String PW = DemoUserSeeder.DEMO_PASSWORD;

    @Autowired MockMvc mvc;

    private ResultActions attemptLogin(String username, String password) throws Exception {
        return mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)));
    }

    private String bearer(String username, String password) throws Exception {
        String body = attemptLogin(username, password).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.accessToken");
    }

    private String fileCase(String auth) throws Exception {
        String body = mvc.perform(post("/api/v1/cases").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Boundary dispute\",\"category\":\"CIVIL\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return String.valueOf((Integer) JsonPath.read(body, "$.id"));
    }

    @Test
    void loginReturnsTokenThatIdentifiesTheUser() throws Exception {
        attemptLogin("registrar", PW)
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("REGISTRAR"))
                .andExpect(jsonPath("$.expiresIn").value(3600));

        mvc.perform(get("/api/v1/auth/me").header("Authorization", bearer("registrar", PW)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("registrar"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_REGISTRAR"));
    }

    @Test
    void requestsWithoutAValidTokenAreRejected() throws Exception {
        mvc.perform(get("/api/v1/cases")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/cases").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordAndUnknownUserGetTheSameMessage() throws Exception {
        attemptLogin("judge", "wrong-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid username or password"));
        attemptLogin("nobody", "whatever")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid username or password"));
    }

    @Test
    void rolesAreEnforced() throws Exception {
        String viewer = bearer("viewer", PW), registrar = bearer("registrar", PW), judge = bearer("judge", PW);

        mvc.perform(post("/api/v1/cases").header("Authorization", viewer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"category\":\"CIVIL\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/cases").header("Authorization", viewer)).andExpect(status().isOk());

        String id = fileCase(registrar);
        for (String s : new String[]{"REGISTERED", "SCHEDULED", "IN_HEARING"}) {
            mvc.perform(patch("/api/v1/cases/" + id + "/status").header("Authorization", registrar)
                    .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"" + s + "\"}"))
                    .andExpect(status().isOk());
        }
        // only a judge may record a judgment
        mvc.perform(patch("/api/v1/cases/" + id + "/status").header("Authorization", registrar)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"JUDGED\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/cases/" + id + "/status").header("Authorization", judge)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"JUDGED\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/employees").header("Authorization", registrar)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/audit").header("Authorization", judge)).andExpect(status().isForbidden());
    }

    @Test
    void auditTrailAndCreatedByRecordWhoDidWhat() throws Exception {
        String registrar = bearer("registrar", PW);
        mvc.perform(get("/api/v1/cases/" + fileCase(registrar)).header("Authorization", registrar))
                .andExpect(jsonPath("$.createdBy").value("registrar"));

        mvc.perform(get("/api/v1/audit").header("Authorization", bearer("admin", PW)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].action", hasItem("CASE_FILED")))
                .andExpect(jsonPath("$.content[*].username", hasItem("registrar")));
    }

    @Test
    void accountLocksAfterFiveFailedAttempts() throws Exception {
        mvc.perform(post("/api/v1/users").header("Authorization", bearer("admin", PW)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"clerk.lock\",\"password\":\"Strong@Pass1\",\"role\":\"REGISTRAR\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        for (int i = 0; i < 5; i++) attemptLogin("clerk.lock", "bad-guess-" + i).andExpect(status().isUnauthorized());
        // even the right password is refused while locked
        attemptLogin("clerk.lock", "Strong@Pass1").andExpect(status().isLocked());
    }

    @Test
    void onlyAdminsCreateUsersAndWeakPasswordsAreRejected() throws Exception {
        String body = "{\"username\":\"new.clerk\",\"password\":\"Strong@Pass1\",\"role\":\"REGISTRAR\"}";
        mvc.perform(post("/api/v1/users").header("Authorization", bearer("registrar", PW))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/users").header("Authorization", bearer("admin", PW)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"weak.clerk\",\"password\":\"password\",\"role\":\"REGISTRAR\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publicPagesAndRequestIds() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(header().exists("X-Request-Id"));
        mvc.perform(get("/actuator/health").header("X-Request-Id", "trace-123"))
                .andExpect(header().string("X-Request-Id", "trace-123"));
        mvc.perform(get("/index.html")).andExpect(status().isOk());
    }
}
