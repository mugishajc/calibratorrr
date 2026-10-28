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
class EmployeeApiTest {
    @Autowired MockMvc mvc;

    @Test
    void fullCrudCycle() throws Exception {
        String body = mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"email\":\"alice@minijust.gov.rw\",\"department\":\"IT\",\"salary\":900000}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = body.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(get("/api/employees/" + id)).andExpect(jsonPath("$.name").value("Alice"));

        mvc.perform(put("/api/employees/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice M\",\"email\":\"alice@minijust.gov.rw\",\"department\":\"IT\",\"salary\":950000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salary").value(950000.0));

        mvc.perform(delete("/api/employees/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/employees/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidEmployee() throws Exception {
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest());
    }
}
