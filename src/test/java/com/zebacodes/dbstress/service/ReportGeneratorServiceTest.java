package com.zebacodes.dbstress.service;

import com.zebacodes.dbstress.model.LatencyStats;
import com.zebacodes.dbstress.model.StressTestResult;
import com.zebacodes.dbstress.model.TestStatus;
import com.zebacodes.dbstress.model.WorkloadType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReportGeneratorServiceTest {

    private final ReportGeneratorService reportGeneratorService = new ReportGeneratorService();

    @Test
    void generatesJsonReportWithExpectedSections() {
        byte[] json = reportGeneratorService.generateJson(result());

        String body = new String(json);
        assertThat(body).contains("configuration");
        assertThat(body).contains("summary");
        assertThat(body).contains("latency");
        assertThat(body).contains("GOOD");
    }

    @Test
    void generatesReadablePdfBytes() {
        byte[] pdf = reportGeneratorService.generatePdf(result());

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    private StressTestResult result() {
        StressTestResult result = new StressTestResult();
        result.setId(UUID.randomUUID());
        result.setTargetDbUrl("jdbc:postgresql://localhost:5432/mydb");
        result.setConcurrentThreads(10);
        result.setDurationSeconds(30);
        result.setRampUpSeconds(5);
        result.setWorkloadType(WorkloadType.MIXED);
        result.setStatus(TestStatus.COMPLETED);
        result.applyFinalMetrics(1200, 2, 40.0, 55.0, new LatencyStats(8.0, 22.0, 45.0));
        return result;
    }
}
