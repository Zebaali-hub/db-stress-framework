package com.zebacodes.dbstress.service;

import com.zebacodes.dbstress.model.LiveMetrics;
import com.zebacodes.dbstress.model.StressTestConfig;
import com.zebacodes.dbstress.model.StressTestResult;
import com.zebacodes.dbstress.model.TestStatus;
import com.zebacodes.dbstress.repository.StressTestResultRepository;
import com.zebacodes.dbstress.service.execution.StressTestRun;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StressTestService {

    private final StressTestResultRepository repository;
    private final MetricsCollector metricsCollector;
    private final WorkloadExecutor workloadExecutor;
    private final WebSocketPublisher webSocketPublisher;
    private final Map<UUID, StressTestRun> activeRuns = new ConcurrentHashMap<>();
    private final Map<UUID, LiveMetrics> latestMetrics = new ConcurrentHashMap<>();

    public StressTestService(StressTestResultRepository repository,
                             MetricsCollector metricsCollector,
                             WorkloadExecutor workloadExecutor,
                             WebSocketPublisher webSocketPublisher) {
        this.repository = repository;
        this.metricsCollector = metricsCollector;
        this.workloadExecutor = workloadExecutor;
        this.webSocketPublisher = webSocketPublisher;
    }

    @Transactional
    public UUID start(StressTestConfig config) {
        validateLifecycleConfig(config);
        UUID testId = UUID.randomUUID();
        StressTestResult result = StressTestResult.fromConfig(testId, config);
        result.setStatus(TestStatus.RUNNING);
        repository.save(result);

        MetricsCollector.TestMetrics metrics = metricsCollector.create(testId, Instant.now());
        StressTestRun run = workloadExecutor.start(
                testId,
                config,
                metrics,
                () -> repository.findById(testId).map(StressTestResult::getStatus).orElse(TestStatus.FAILED),
                snapshot -> {
                    latestMetrics.put(testId, snapshot);
                    webSocketPublisher.publish(testId, snapshot);
                }
        );
        activeRuns.put(testId, run);
        run.completion().thenAccept(summary -> finalizeResult(testId, summary));
        return testId;
    }

    @Transactional(readOnly = true)
    public LiveMetrics status(UUID testId) {
        return latestMetrics.computeIfAbsent(testId, id -> {
            StressTestResult result = getResult(id);
            return new LiveMetrics(
                    id,
                    result.getAvgTps().doubleValue(),
                    0.0,
                    result.getP50LatencyMs().doubleValue(),
                    result.getP95LatencyMs().doubleValue(),
                    result.getP99LatencyMs().doubleValue(),
                    result.getTotalTransactions(),
                    result.getTotalErrors(),
                    activeRuns.getOrDefault(id, emptyRun()).activeThreads(),
                    0,
                    result.getStatus()
            );
        });
    }

    @Transactional
    public LiveMetrics stop(UUID testId) {
        StressTestResult result = getResult(testId);
        if (result.getStatus() != TestStatus.RUNNING) {
            return status(testId);
        }
        result.setStatus(TestStatus.STOPPED);
        repository.save(result);
        StressTestRun run = activeRuns.get(testId);
        if (run != null) {
            run.stop();
        }
        return status(testId);
    }

    @Transactional(readOnly = true)
    public StressTestResult getResult(UUID testId) {
        return repository.findById(testId)
                .orElseThrow(() -> new EntityNotFoundException("Stress test not found: " + testId));
    }

    @Transactional(readOnly = true)
    public List<StressTestResult> history() {
        return repository.findTop10ByOrderByStartedAtDesc();
    }

    @Transactional
    public void finalizeResult(UUID testId, com.zebacodes.dbstress.service.execution.ExecutionSummary summary) {
        StressTestResult result = getResult(testId);
        TestStatus storedStatus = result.getStatus();
        TestStatus finalStatus = storedStatus == TestStatus.STOPPED ? TestStatus.STOPPED : summary.status();
        result.applyFinalMetrics(
                summary.totalTransactions(),
                summary.totalErrors(),
                summary.averageTps(),
                summary.peakTps(),
                summary.latencyStats()
        );
        result.setStatus(finalStatus);
        repository.save(result);
        activeRuns.remove(testId);
    }

    private void validateLifecycleConfig(StressTestConfig config) {
        if (config.getRampUpSeconds() > config.getDurationSeconds()) {
            throw new IllegalArgumentException("Ramp-up seconds cannot exceed duration seconds");
        }
    }

    private StressTestRun emptyRun() {
        return new StressTestRun(new java.util.concurrent.CountDownLatch(0), new java.util.concurrent.atomic.AtomicInteger(),
                new java.util.concurrent.atomic.AtomicReference<>(), java.util.concurrent.CompletableFuture.completedFuture(null));
    }
}
