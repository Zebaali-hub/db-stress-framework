package com.zebacodes.dbstress.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zebacodes.dbstress.model.LiveMetrics;
import com.zebacodes.dbstress.model.StressTestConfig;
import com.zebacodes.dbstress.model.StressTestResult;
import com.zebacodes.dbstress.model.TestStatus;
import com.zebacodes.dbstress.model.WorkloadType;
import com.zebacodes.dbstress.service.ReportGeneratorService;
import com.zebacodes.dbstress.service.StressTestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {StressTestController.class, ApiExceptionHandler.class})
class StressTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StressTestService stressTestService;

    @MockBean
    private ReportGeneratorService reportGeneratorService;

    @Test
    void startReturnsAcceptedTestId() throws Exception {
        UUID id = UUID.randomUUID();
        when(stressTestService.start(any())).thenReturn(id);

        mockMvc.perform(post("/api/stress/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config())))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.testId").value(id.toString()))
                .andExpect(jsonPath("$.dashboardUrl").value("/dashboard/" + id));
    }

    @Test
    void invalidStartReturnsBadRequest() throws Exception {
        StressTestConfig config = config();
        config.setJdbcUrl("");

        mockMvc.perform(post("/api/stress/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.jdbcUrl").exists());
    }

    @Test
    void statusReturnsLatestMetrics() throws Exception {
        UUID id = UUID.randomUUID();
        when(stressTestService.status(id)).thenReturn(new LiveMetrics(id, 10, 0, 1, 2, 3, 100, 0, 4, 5, TestStatus.RUNNING));

        mockMvc.perform(get("/api/stress/{id}/status", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentTps").value(10.0))
                .andExpect(jsonPath("$.status").value("RUNNING"));
    }

    @Test
    void historyReturnsResults() throws Exception {
        StressTestResult result = new StressTestResult();
        result.setId(UUID.randomUUID());
        result.setTargetDbUrl("jdbc:postgresql://localhost/db");
        result.setWorkloadType(WorkloadType.READ_HEAVY);
        result.setStatus(TestStatus.COMPLETED);
        when(stressTestService.history()).thenReturn(List.of(result));

        mockMvc.perform(get("/api/stress/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].targetDbUrl").value("jdbc:postgresql://localhost/db"));
    }

    @Test
    void downloadsJsonReport() throws Exception {
        UUID id = UUID.randomUUID();
        StressTestResult result = new StressTestResult();
        result.setId(id);
        when(stressTestService.getResult(id)).thenReturn(result);
        when(reportGeneratorService.generateJson(result)).thenReturn("{\"ok\":true}".getBytes());

        mockMvc.perform(get("/api/stress/{id}/report/json", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    private StressTestConfig config() {
        StressTestConfig config = new StressTestConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
        config.setUsername("user");
        config.setPassword("pass");
        config.setConcurrentThreads(5);
        config.setDurationSeconds(10);
        config.setRampUpSeconds(1);
        config.setWorkloadType(WorkloadType.MIXED);
        return config;
    }
}
