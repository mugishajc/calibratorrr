package rw.minijust.iecms.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rw.minijust.iecms.model.CaseStatus;
import rw.minijust.iecms.security.SecurityConfig;
import rw.minijust.iecms.service.CaseService;
import rw.minijust.iecms.service.NotFoundException;

/** Web-layer slice test: only the controller and error handler are loaded, the service is mocked. */
@WebMvcTest(CaseController.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = {"REGISTRAR", "JUDGE"})
class CaseControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean CaseService service;

    @Test
    void illegalTransitionReturns409WithProblemDetail() throws Exception {
        when(service.changeStatus(1L, CaseStatus.JUDGED))
                .thenThrow(new IllegalStateException("Cannot move case from FILED to JUDGED"));

        mvc.perform(patch("/api/v1/cases/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"JUDGED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Cannot move case from FILED to JUDGED"));
    }

    @Test
    void missingCaseReturns404() throws Exception {
        when(service.get(5L)).thenThrow(new NotFoundException("Case 5 not found"));

        mvc.perform(get("/api/v1/cases/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Case 5 not found"));
    }

    @Test
    void hearingInThePastIsRejected() throws Exception {
        mvc.perform(post("/api/v1/cases/1/hearings").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scheduledAt\":\"2020-01-01T09:00:00\",\"courtRoom\":\"Room A\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("scheduledAt")));
    }

    @Test
    void missingStatusIsRejected() throws Exception {
        when(service.changeStatus(any(), any())).thenThrow(new AssertionError("should not be called"));

        mvc.perform(patch("/api/v1/cases/1/status").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
