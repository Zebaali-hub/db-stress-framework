package com.zebacodes.dbstress.service;

import com.zebacodes.dbstress.model.LatencyStats;
import com.zebacodes.dbstress.model.LiveMetrics;
import com.zebacodes.dbstress.model.TestStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsCollectorTest {

    @Test
    void snapshotCalculatesTpsErrorsAndPercentiles() {
        MetricsCollector.TestMetrics metrics = new MetricsCollector().create(UUID.randomUUID(), Instant.now());

        for (double latency : new double[]{1, 2, 3, 4, 5, 100}) {
            metrics.recordLatency(latency);
            metrics.incrementTransactions();
        }
        metrics.incrementErrors();

        LiveMetrics snapshot = metrics.snapshot(3, TestStatus.RUNNING);

        assertThat(snapshot.getCurrentTps()).isEqualTo(6.0);
        assertThat(snapshot.getTotalTransactions()).isEqualTo(6);
        assertThat(snapshot.getTotalErrors()).isEqualTo(1);
        assertThat(snapshot.getErrorRate()).isCloseTo(14.28, withinPercentage(1));
        assertThat(snapshot.getP50LatencyMs()).isEqualTo(3.0);
        assertThat(snapshot.getP95LatencyMs()).isEqualTo(100.0);
        assertThat(snapshot.getP99LatencyMs()).isEqualTo(100.0);
    }

    @Test
    void finalLatencyStatsUseRetainedSamples() {
        MetricsCollector.TestMetrics metrics = new MetricsCollector().create(UUID.randomUUID(), Instant.now());
        metrics.recordLatency(10);
        metrics.recordLatency(20);
        metrics.recordLatency(30);
        metrics.recordLatency(40);

        LatencyStats stats = metrics.finalLatencyStats();

        assertThat(stats.p50LatencyMs()).isEqualTo(20.0);
        assertThat(stats.p95LatencyMs()).isEqualTo(40.0);
        assertThat(metrics.getTotalLatencySamples()).isEqualTo(4);
    }

    private org.assertj.core.data.Offset<Double> withinPercentage(double percentage) {
        return org.assertj.core.data.Offset.offset(percentage);
    }
}
