package rw.minijust.iecms;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = "ADMIN")
class CaseApiTest {
    @Autowired MockMvc mvc;

    @Test
    void filesCaseAndRejectsInvalidInput() throws Exception {
        mvc.perform(post("/api/v1/cases").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Contract breach\",\"category\":\"COMMERCIAL\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FILED"))
                .andExpect(jsonPath("$.caseNumber").exists());

        mvc.perform(post("/api/v1/cases").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"category\":\"UNKNOWN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownCaseReturns404() throws Exception {
        mvc.perform(get("/api/v1/cases/9999")).andExpect(status().isNotFound());
    }

    @Test
    void fullCaseLifecycle() throws Exception {
        String body = mvc.perform(post("/api/v1/cases").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Land dispute\",\"category\":\"CIVIL\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = body.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(patch("/api/v1/cases/" + id + "/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"REGISTERED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REGISTERED"));

        String hearing = "{\"scheduledAt\":\"2099-01-15T09:00:00\",\"courtRoom\":\"Room " + id + "\"}";
        mvc.perform(post("/api/v1/cases/" + id + "/hearings").contentType(MediaType.APPLICATION_JSON).content(hearing))
                .andExpect(status().isCreated());
        // same room, same time: double booking is refused
        mvc.perform(post("/api/v1/cases/" + id + "/hearings").contentType(MediaType.APPLICATION_JSON).content(hearing))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/v1/cases/" + id))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.hearings").value(1));

        mvc.perform(get("/api/v1/cases").param("status", "SCHEDULED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("SCHEDULED"));

        mvc.perform(get("/api/v1/cases/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SCHEDULED").isNumber());
    }
}
