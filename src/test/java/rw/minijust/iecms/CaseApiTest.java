package rw.minijust.iecms;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
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
}
