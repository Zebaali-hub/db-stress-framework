package com.zebacodes.dbstress.controller;

import com.zebacodes.dbstress.model.LiveMetrics;
import com.zebacodes.dbstress.model.StressTestConfig;
import com.zebacodes.dbstress.model.StressTestResult;
import com.zebacodes.dbstress.service.ReportGeneratorService;
import com.zebacodes.dbstress.service.StressTestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/stress")
public class StressTestController {

    private final StressTestService stressTestService;
    private final ReportGeneratorService reportGeneratorService;

    public StressTestController(StressTestService stressTestService, ReportGeneratorService reportGeneratorService) {
        this.stressTestService = stressTestService;
        this.reportGeneratorService = reportGeneratorService;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> start(@Valid @RequestBody StressTestConfig config) {
        UUID testId = stressTestService.start(config);
        return ResponseEntity.accepted().body(Map.of(
                "testId", testId,
                "dashboardUrl", "/dashboard/" + testId
        ));
    }

    @GetMapping("/{id}/status")
    public LiveMetrics status(@PathVariable UUID id) {
        return stressTestService.status(id);
    }

    @PostMapping("/{id}/stop")
    public LiveMetrics stop(@PathVariable UUID id) {
        return stressTestService.stop(id);
    }

    @GetMapping("/{id}/report/json")
    public ResponseEntity<byte[]> jsonReport(@PathVariable UUID id) {
        StressTestResult result = stressTestService.getResult(id);
        byte[] report = reportGeneratorService.generateJson(result);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"stress-report-" + id + ".json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(report);
    }

    @GetMapping("/{id}/report/pdf")
    public ResponseEntity<byte[]> pdfReport(@PathVariable UUID id) {
        StressTestResult result = stressTestService.getResult(id);
        byte[] report = reportGeneratorService.generatePdf(result);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"stress-report-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(report);
    }

    @GetMapping("/history")
    public List<StressTestResult> history() {
        return stressTestService.history();
    }
}
