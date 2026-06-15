package com.zebacodes.dbstress.model;

public record LatencyStats(double p50LatencyMs, double p95LatencyMs, double p99LatencyMs) {

    public static LatencyStats empty() {
        return new LatencyStats(0.0, 0.0, 0.0);
    }
}
