package com.zebacodes.dbstress.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.zebacodes.dbstress.model.StressTestResult;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportGeneratorService {

    private final ObjectMapper objectMapper;

    public ReportGeneratorService() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public byte[] generateJson(StressTestResult result) {
        try {
            return objectMapper.writeValueAsBytes(reportMap(result));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to generate JSON report", e);
        }
    }

    public byte[] generatePdf(StressTestResult result) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfDocument pdfDocument = new PdfDocument(new PdfWriter(out));
            try (Document document = new Document(pdfDocument)) {
                document.add(new Paragraph("DATABASE STRESS TEST REPORT").setBold().setFontSize(18));
                document.add(new Paragraph("Generated: " + OffsetDateTime.now()));
                document.add(new Paragraph("Test ID: " + result.getId()));
                document.add(new Paragraph("Target Database: " + result.getTargetDbUrl()));
                document.add(section("TEST CONFIGURATION"));
                document.add(table(Map.of(
                        "Concurrent Threads", result.getConcurrentThreads(),
                        "Duration", result.getDurationSeconds() + " seconds",
                        "Ramp-up Period", result.getRampUpSeconds() + " seconds",
                        "Workload Type", result.getWorkloadType(),
                        "Workload Intent", workloadIntent(result)
                )));
                if (result.isOracleDiagnosticsEnabled()) {
                    document.add(section("ORACLE DIAGNOSTIC CONTEXT"));
                    document.add(table(Map.of(
                            "AWR Begin Snapshot", valueOrBlank(result.getAwrBeginSnapshot()),
                            "AWR End Snapshot", valueOrBlank(result.getAwrEndSnapshot()),
                            "SQL Monitor", valueOrBlank(result.getSqlMonitorReference()),
                            "Trace File", valueOrBlank(result.getTraceFileReference()),
                            "Notes", valueOrBlank(result.getDiagnosticNotes())
                    )));
                }
                document.add(section("RESULTS SUMMARY"));
                document.add(table(Map.of(
                        "Total Transactions", result.getTotalTransactions(),
                        "Total Errors", result.getTotalErrors(),
                        "Error Rate", errorRate(result) + "%",
                        "Average TPS", result.getAvgTps(),
                        "Peak TPS", result.getPeakTps()
                )));
                document.add(section("LATENCY DISTRIBUTION"));
                document.add(table(Map.of(
                        "P50 Median", result.getP50LatencyMs() + " ms",
                        "P95", result.getP95LatencyMs() + " ms",
                        "P99", result.getP99LatencyMs() + " ms"
                )));
                document.add(section("PERFORMANCE ASSESSMENT"));
                document.add(new Paragraph("Grade: " + grade(result)));
                document.add(section("RECOMMENDATIONS"));
                recommendations(result).forEach(item -> document.add(new Paragraph("- " + item)));
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to generate PDF report", e);
        }
    }

    public Map<String, Object> reportMap(StressTestResult result) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("testId", result.getId());
        report.put("generatedAt", OffsetDateTime.now());
        report.put("targetDatabase", result.getTargetDbUrl());
        report.put("configuration", Map.of(
                "concurrentThreads", result.getConcurrentThreads(),
                "durationSeconds", result.getDurationSeconds(),
                "rampUpSeconds", result.getRampUpSeconds(),
                "workloadType", result.getWorkloadType(),
                "workloadIntent", workloadIntent(result)
        ));
        report.put("oracleDiagnostics", Map.of(
                "enabled", result.isOracleDiagnosticsEnabled(),
                "awrBeginSnapshot", valueOrBlank(result.getAwrBeginSnapshot()),
                "awrEndSnapshot", valueOrBlank(result.getAwrEndSnapshot()),
                "sqlMonitorReference", valueOrBlank(result.getSqlMonitorReference()),
                "traceFileReference", valueOrBlank(result.getTraceFileReference()),
                "diagnosticNotes", valueOrBlank(result.getDiagnosticNotes())
        ));
        report.put("summary", Map.of(
                "totalTransactions", result.getTotalTransactions(),
                "totalErrors", result.getTotalErrors(),
                "errorRatePercent", errorRate(result),
                "averageTps", result.getAvgTps(),
                "peakTps", result.getPeakTps()
        ));
        report.put("latency", Map.of(
                "p50Ms", result.getP50LatencyMs(),
                "p95Ms", result.getP95LatencyMs(),
                "p99Ms", result.getP99LatencyMs()
        ));
        report.put("grade", grade(result));
        report.put("recommendations", recommendations(result));
        return report;
    }

    public String grade(StressTestResult result) {
        BigDecimal p99 = result.getP99LatencyMs();
        if (p99.compareTo(BigDecimal.TEN) < 0) {
            return "EXCELLENT";
        }
        if (p99.compareTo(BigDecimal.valueOf(50)) < 0) {
            return "GOOD";
        }
        if (p99.compareTo(BigDecimal.valueOf(200)) < 0) {
            return "ACCEPTABLE";
        }
        return "POOR";
    }

    public List<String> recommendations(StressTestResult result) {
        double errors = result.getTotalErrors();
        double total = result.getTotalTransactions() + errors;
        double errorRate = total == 0 ? 0.0 : errors * 100.0 / total;
        if (errorRate > 5.0) {
            return List.of("Error rate is above 5%. Reduce concurrency or inspect target database errors before increasing load.");
        }
        if (result.getP99LatencyMs().compareTo(BigDecimal.valueOf(200)) >= 0) {
            return List.of("High p99 latency suggests connection pool saturation, lock contention, or overloaded storage. Check wait events and reduce thread count.");
        }
        if (result.getPeakTps().compareTo(result.getAvgTps().multiply(BigDecimal.valueOf(1.5))) > 0) {
            return List.of("Peak TPS is much higher than average TPS. Review ramp-up behavior and target database warm-up effects.");
        }
        return List.of("Results look stable. Increase concurrency gradually to find the saturation point.");
    }

    public String workloadIntent(StressTestResult result) {
        return switch (result.getWorkloadType()) {
            case READ_HEAVY -> "Read throughput and SELECT latency under concurrent backend sessions.";
            case WRITE_HEAVY -> "Write throughput, DML pressure, and index maintenance under load.";
            case MIXED -> "Normal backend traffic mix: reads, writes, and read-after-write behavior.";
            case TRANSACTION_HEAVY -> "Commit/rollback paths, transaction sequencing, and lock behavior.";
            case FEATURE_PARTITIONING -> "Partition/range-key style access, pruning-like patterns, and partitioned-table pressure.";
            case FEATURE_INDEXING -> "Index lookup, range scan, random access, and index maintenance behavior.";
            case FEATURE_CACHE_CONTENTION -> "Hot row/range access to mimic buffer cache and hot block contention.";
            case FEATURE_MVCC_CONCURRENCY -> "Concurrent readers/writers, MVCC visibility, rollback, and lock interaction.";
            case FEATURE_QUERY_OPTIMIZER -> "Optimizer-sensitive filters, scans, and aggregation-heavy SQL behavior.";
            case FEATURE_VECTOR_SEARCH -> "Vector/top-k style access pattern; attach real pgvector/Oracle Vector SQL through custom queries for exact benchmarking.";
            case TPC_C_LIKE -> "OLTP-style new-order/payment/status transaction mix inspired by TPC-C.";
            case TPC_H_LIKE -> "Analytical scan/filter/aggregation workload inspired by TPC-H reporting queries.";
        };
    }

    private Paragraph section(String title) {
        return new Paragraph(title).setBold().setFontSize(13).setMarginTop(18);
    }

    private Table table(Map<String, ?> rows) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{35, 65})).useAllAvailableWidth();
        rows.forEach((key, value) -> {
            table.addCell(new Paragraph(key).setBold());
            table.addCell(new Paragraph(String.valueOf(value)));
        });
        return table;
    }

    private BigDecimal errorRate(StressTestResult result) {
        BigDecimal total = BigDecimal.valueOf(result.getTotalTransactions() + result.getTotalErrors());
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(result.getTotalErrors())
                .multiply(BigDecimal.valueOf(100))
                .divide(total, 2, RoundingMode.HALF_UP);
    }

    private String valueOrBlank(String value) {
        return value == null || value.isBlank() ? "Not provided" : value;
    }
}
