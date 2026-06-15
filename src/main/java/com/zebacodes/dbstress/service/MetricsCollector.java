package com.zebacodes.dbstress.service;

import com.zebacodes.dbstress.model.LatencyStats;
import com.zebacodes.dbstress.model.LiveMetrics;
import com.zebacodes.dbstress.model.TestStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAccumulator;

@Component
public class MetricsCollector {

    private static final int MAX_FINAL_LATENCY_SAMPLES = 100_000;

    public TestMetrics create(UUID testId, Instant startedAt) {
        return new TestMetrics(testId, startedAt);
    }

    public static class TestMetrics {

        private final UUID testId;
        private final Instant startedAt;
        private final Queue<Double> latencyWindow = new ConcurrentLinkedQueue<>();
        private final ConcurrentLinkedDeque<Double> finalLatencySamples = new ConcurrentLinkedDeque<>();
        private final AtomicLong totalTransactions = new AtomicLong();
        private final AtomicLong totalErrors = new AtomicLong();
        private final AtomicLong windowTransactions = new AtomicLong();
        private final AtomicLong windowErrors = new AtomicLong();
        private final AtomicLong totalLatencySamples = new AtomicLong();
        private final AtomicInteger retainedLatencySamples = new AtomicInteger();
        private final DoubleAccumulator peakTps = new DoubleAccumulator(Double::max, 0.0);

        private TestMetrics(UUID testId, Instant startedAt) {
            this.testId = testId;
            this.startedAt = startedAt;
        }

        public void recordLatency(double latencyMs) {
            if (latencyMs < 0.0 || Double.isNaN(latencyMs) || Double.isInfinite(latencyMs)) {
                return;
            }
            latencyWindow.offer(latencyMs);
            finalLatencySamples.offer(latencyMs);
            totalLatencySamples.incrementAndGet();
            int retained = retainedLatencySamples.incrementAndGet();
            while (retained > MAX_FINAL_LATENCY_SAMPLES) {
                if (finalLatencySamples.poll() == null) {
                    break;
                }
                retained = retainedLatencySamples.decrementAndGet();
            }
        }

        public void incrementTransactions() {
            totalTransactions.incrementAndGet();
            windowTransactions.incrementAndGet();
        }

        public void incrementErrors() {
            totalErrors.incrementAndGet();
            windowErrors.incrementAndGet();
        }

        public LiveMetrics snapshot(int activeThreads, TestStatus status) {
            long txInWindow = windowTransactions.getAndSet(0);
            long errorsInWindow = windowErrors.getAndSet(0);
            double currentTps = txInWindow;
            peakTps.accumulate(currentTps);

            List<Double> sortedWindow = drainWindow();
            LatencyStats stats = latencyStats(sortedWindow);
            double errorRate = txInWindow + errorsInWindow == 0
                    ? 0.0
                    : (errorsInWindow * 100.0) / (txInWindow + errorsInWindow);

            return new LiveMetrics(
                    testId,
                    currentTps,
                    errorRate,
                    stats.p50LatencyMs(),
                    stats.p95LatencyMs(),
                    stats.p99LatencyMs(),
                    totalTransactions.get(),
                    totalErrors.get(),
                    activeThreads,
                    elapsedSeconds(),
                    status
            );
        }

        public long getTotalTransactions() {
            return totalTransactions.get();
        }

        public long getTotalErrors() {
            return totalErrors.get();
        }

        public double getPeakTps() {
            return peakTps.get();
        }

        public double averageTps() {
            long elapsed = Math.max(1, elapsedSeconds());
            return totalTransactions.get() / (double) elapsed;
        }

        public LatencyStats finalLatencyStats() {
            List<Double> sorted = new ArrayList<>(finalLatencySamples);
            Collections.sort(sorted);
            return latencyStats(sorted);
        }

        public long elapsedSeconds() {
            return Math.max(0, Duration.between(startedAt, Instant.now()).toSeconds());
        }

        public long getTotalLatencySamples() {
            return totalLatencySamples.get();
        }

        private List<Double> drainWindow() {
            List<Double> values = new ArrayList<>();
            Double value;
            while ((value = latencyWindow.poll()) != null) {
                values.add(value);
            }
            Collections.sort(values);
            return values;
        }

        private LatencyStats latencyStats(List<Double> sorted) {
            if (sorted.isEmpty()) {
                return LatencyStats.empty();
            }
            return new LatencyStats(
                    percentile(sorted, 50),
                    percentile(sorted, 95),
                    percentile(sorted, 99)
            );
        }

        private double percentile(List<Double> sorted, int pct) {
            int index = (int) Math.ceil(pct / 100.0 * sorted.size()) - 1;
            return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
        }
    }
}
