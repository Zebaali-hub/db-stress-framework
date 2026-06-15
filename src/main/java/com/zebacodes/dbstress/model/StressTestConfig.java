package com.zebacodes.dbstress.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class StressTestConfig {

    @NotBlank(message = "JDBC URL is required")
    @Size(max = 500, message = "JDBC URL must be 500 characters or less")
    private String jdbcUrl;

    @NotBlank(message = "Username is required")
    private String username;

    @NotNull(message = "Password must be provided")
    private String password;

    @Min(value = 1, message = "Concurrent threads must be at least 1")
    @Max(value = 500, message = "Concurrent threads cannot exceed 500")
    private int concurrentThreads = 10;

    @Min(value = 1, message = "Duration must be at least 1 second")
    @Max(value = 86_400, message = "Duration cannot exceed 24 hours")
    private int durationSeconds = 30;

    @Min(value = 0, message = "Ramp-up cannot be negative")
    @Max(value = 86_400, message = "Ramp-up cannot exceed 24 hours")
    private int rampUpSeconds = 5;

    @NotNull(message = "Workload type is required")
    private WorkloadType workloadType = WorkloadType.MIXED;

    private List<@NotBlank(message = "Custom queries cannot contain blank entries") String> customQueries = new ArrayList<>();

    private boolean oracleDiagnosticsEnabled;

    @Size(max = 100, message = "AWR begin snapshot must be 100 characters or less")
    private String awrBeginSnapshot;

    @Size(max = 100, message = "AWR end snapshot must be 100 characters or less")
    private String awrEndSnapshot;

    @Size(max = 500, message = "SQL Monitor reference must be 500 characters or less")
    private String sqlMonitorReference;

    @Size(max = 500, message = "Trace file reference must be 500 characters or less")
    private String traceFileReference;

    @Size(max = 2000, message = "Diagnostic notes must be 2000 characters or less")
    private String diagnosticNotes;

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public List<String> getCustomQueries() {
        return customQueries;
    }

    public void setCustomQueries(List<String> customQueries) {
        this.customQueries = customQueries == null ? new ArrayList<>() : customQueries;
    }

    public boolean hasCustomQueries() {
        return customQueries != null && customQueries.stream().anyMatch(query -> query != null && !query.isBlank());
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
