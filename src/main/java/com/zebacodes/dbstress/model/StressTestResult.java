package com.zebacodes.dbstress.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "stress_test_results")
public class StressTestResult {

    @Id
    private UUID id;

    @Column(name = "target_db_url", nullable = false, length = 500)
    private String targetDbUrl;

    @Column(name = "concurrent_threads", nullable = false)
    private int concurrentThreads;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(name = "ramp_up_seconds", nullable = false)
    private int rampUpSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "workload_type", nullable = false, length = 50)
    private WorkloadType workloadType;

    @Column(name = "total_transactions")
    private long totalTransactions;

    @Column(name = "total_errors")
    private long totalErrors;

    @Column(name = "avg_tps", precision = 10, scale = 2)
    private BigDecimal avgTps = BigDecimal.ZERO;

    @Column(name = "peak_tps", precision = 10, scale = 2)
    private BigDecimal peakTps = BigDecimal.ZERO;

    @Column(name = "p50_latency_ms", precision = 10, scale = 2)
    private BigDecimal p50LatencyMs = BigDecimal.ZERO;

    @Column(name = "p95_latency_ms", precision = 10, scale = 2)
    private BigDecimal p95LatencyMs = BigDecimal.ZERO;

    @Column(name = "p99_latency_ms", precision = 10, scale = 2)
    private BigDecimal p99LatencyMs = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TestStatus status = TestStatus.PENDING;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "oracle_diagnostics_enabled")
    private boolean oracleDiagnosticsEnabled;

    @Column(name = "awr_begin_snapshot", length = 100)
    private String awrBeginSnapshot;

    @Column(name = "awr_end_snapshot", length = 100)
    private String awrEndSnapshot;

    @Column(name = "sql_monitor_reference", length = 500)
    private String sqlMonitorReference;

    @Column(name = "trace_file_reference", length = 500)
    private String traceFileReference;

    @Column(name = "diagnostic_notes", length = 2000)
    private String diagnosticNotes;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (startedAt == null) {
            startedAt = OffsetDateTime.now();
        }
    }

    public static StressTestResult fromConfig(UUID id, StressTestConfig config) {
        StressTestResult result = new StressTestResult();
        result.setId(id);
        result.setTargetDbUrl(config.getJdbcUrl());
        result.setConcurrentThreads(config.getConcurrentThreads());
        result.setDurationSeconds(config.getDurationSeconds());
        result.setRampUpSeconds(config.getRampUpSeconds());
        result.setWorkloadType(config.getWorkloadType());
        result.setStatus(TestStatus.PENDING);
        result.setStartedAt(OffsetDateTime.now());
        result.setOracleDiagnosticsEnabled(config.isOracleDiagnosticsEnabled());
        result.setAwrBeginSnapshot(config.getAwrBeginSnapshot());
        result.setAwrEndSnapshot(config.getAwrEndSnapshot());
        result.setSqlMonitorReference(config.getSqlMonitorReference());
        result.setTraceFileReference(config.getTraceFileReference());
        result.setDiagnosticNotes(config.getDiagnosticNotes());
        return result;
    }

    public void applyFinalMetrics(long transactions, long errors, double avgTps, double peakTps, LatencyStats latencyStats) {
        this.totalTransactions = transactions;
        this.totalErrors = errors;
        this.avgTps = decimal(avgTps);
        this.peakTps = decimal(peakTps);
        this.p50LatencyMs = decimal(latencyStats.p50LatencyMs());
        this.p95LatencyMs = decimal(latencyStats.p95LatencyMs());
        this.p99LatencyMs = decimal(latencyStats.p99LatencyMs());
        this.completedAt = OffsetDateTime.now();
    }

    private BigDecimal decimal(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTargetDbUrl() {
        return targetDbUrl;
    }

    public void setTargetDbUrl(String targetDbUrl) {
        this.targetDbUrl = targetDbUrl;
    }

    public int getConcurrentThreads() {
        return concurrentThreads;
    }

    public void setConcurrentThreads(int concurrentThreads) {
        this.concurrentThreads = concurrentThreads;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public int getRampUpSeconds() {
        return rampUpSeconds;
    }

    public void setRampUpSeconds(int rampUpSeconds) {
        this.rampUpSeconds = rampUpSeconds;
    }

    public WorkloadType getWorkloadType() {
        return workloadType;
    }

    public void setWorkloadType(WorkloadType workloadType) {
        this.workloadType = workloadType;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public long getTotalErrors() {
        return totalErrors;
    }

    public void setTotalErrors(long totalErrors) {
        this.totalErrors = totalErrors;
    }

    public BigDecimal getAvgTps() {
        return avgTps;
    }

    public void setAvgTps(BigDecimal avgTps) {
        this.avgTps = avgTps;
    }

    public BigDecimal getPeakTps() {
        return peakTps;
    }

    public void setPeakTps(BigDecimal peakTps) {
        this.peakTps = peakTps;
    }

    public BigDecimal getP50LatencyMs() {
        return p50LatencyMs;
    }

    public void setP50LatencyMs(BigDecimal p50LatencyMs) {
        this.p50LatencyMs = p50LatencyMs;
    }

    public BigDecimal getP95LatencyMs() {
        return p95LatencyMs;
    }

    public void setP95LatencyMs(BigDecimal p95LatencyMs) {
        this.p95LatencyMs = p95LatencyMs;
    }

    public BigDecimal getP99LatencyMs() {
        return p99LatencyMs;
    }

    public void setP99LatencyMs(BigDecimal p99LatencyMs) {
        this.p99LatencyMs = p99LatencyMs;
    }

    public TestStatus getStatus() {
        return status;
    }

    public void setStatus(TestStatus status) {
        this.status = status;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public boolean isOracleDiagnosticsEnabled() {
        return oracleDiagnosticsEnabled;
    }

    public void setOracleDiagnosticsEnabled(boolean oracleDiagnosticsEnabled) {
        this.oracleDiagnosticsEnabled = oracleDiagnosticsEnabled;
    }

    public String getAwrBeginSnapshot() {
        return awrBeginSnapshot;
    }

    public void setAwrBeginSnapshot(String awrBeginSnapshot) {
        this.awrBeginSnapshot = awrBeginSnapshot;
    }

    public String getAwrEndSnapshot() {
        return awrEndSnapshot;
    }

    public void setAwrEndSnapshot(String awrEndSnapshot) {
        this.awrEndSnapshot = awrEndSnapshot;
    }

    public String getSqlMonitorReference() {
        return sqlMonitorReference;
    }

    public void setSqlMonitorReference(String sqlMonitorReference) {
        this.sqlMonitorReference = sqlMonitorReference;
    }

    public String getTraceFileReference() {
        return traceFileReference;
    }

    public void setTraceFileReference(String traceFileReference) {
        this.traceFileReference = traceFileReference;
    }

    public String getDiagnosticNotes() {
        return diagnosticNotes;
    }

    public void setDiagnosticNotes(String diagnosticNotes) {
        this.diagnosticNotes = diagnosticNotes;
    }
}
