package com.zebacodes.dbstress.model;

import java.time.Instant;
import java.util.UUID;

public class LiveMetrics {

    private UUID testId;
    private double currentTps;
    private double errorRate;
    private double p50LatencyMs;
    private double p95LatencyMs;
    private double p99LatencyMs;
    private long totalTransactions;
    private long totalErrors;
    private int activeThreads;
    private long elapsedSeconds;
    private TestStatus status;
    private Instant capturedAt;

    public LiveMetrics() {
    }

    public LiveMetrics(UUID testId, double currentTps, double errorRate, double p50LatencyMs,
                       double p95LatencyMs, double p99LatencyMs, long totalTransactions,
                       long totalErrors, int activeThreads, long elapsedSeconds, TestStatus status) {
        this.testId = testId;
        this.currentTps = currentTps;
        this.errorRate = errorRate;
        this.p50LatencyMs = p50LatencyMs;
        this.p95LatencyMs = p95LatencyMs;
        this.p99LatencyMs = p99LatencyMs;
        this.totalTransactions = totalTransactions;
        this.totalErrors = totalErrors;
        this.activeThreads = activeThreads;
        this.elapsedSeconds = elapsedSeconds;
        this.status = status;
        this.capturedAt = Instant.now();
    }

    public UUID getTestId() {
        return testId;
    }

    public void setTestId(UUID testId) {
        this.testId = testId;
    }

    public double getCurrentTps() {
        return currentTps;
    }

    public void setCurrentTps(double currentTps) {
        this.currentTps = currentTps;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public double getP50LatencyMs() {
        return p50LatencyMs;
    }

    public void setP50LatencyMs(double p50LatencyMs) {
        this.p50LatencyMs = p50LatencyMs;
    }

    public double getP95LatencyMs() {
        return p95LatencyMs;
    }

    public void setP95LatencyMs(double p95LatencyMs) {
        this.p95LatencyMs = p95LatencyMs;
    }

    public double getP99LatencyMs() {
        return p99LatencyMs;
    }

    public void setP99LatencyMs(double p99LatencyMs) {
        this.p99LatencyMs = p99LatencyMs;
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

    public int getActiveThreads() {
        return activeThreads;
    }

    public void setActiveThreads(int activeThreads) {
        this.activeThreads = activeThreads;
    }

    public long getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(long elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    public TestStatus getStatus() {
        return status;
    }

    public void setStatus(TestStatus status) {
        this.status = status;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(Instant capturedAt) {
        this.capturedAt = capturedAt;
    }
}
