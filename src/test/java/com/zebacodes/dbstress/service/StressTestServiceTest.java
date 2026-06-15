package com.zebacodes.dbstress.service;

import com.zebacodes.dbstress.model.LatencyStats;
import com.zebacodes.dbstress.model.StressTestConfig;
import com.zebacodes.dbstress.model.StressTestResult;
import com.zebacodes.dbstress.model.TestStatus;
import com.zebacodes.dbstress.model.WorkloadType;
import com.zebacodes.dbstress.repository.StressTestResultRepository;
import com.zebacodes.dbstress.service.execution.ExecutionSummary;
import com.zebacodes.dbstress.service.execution.StressTestRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StressTestServiceTest {

    @Mock
    private StressTestResultRepository repository;
    @Mock
    private MetricsCollector metricsCollector;
    @Mock
    private WorkloadExecutor workloadExecutor;
    @Mock
    private WebSocketPublisher webSocketPublisher;

    private StressTestService service;

    @BeforeEach
    void setUp() {
        service = new StressTestService(repository, metricsCollector, workloadExecutor, webSocketPublisher);
    }

    @Test
    void startPersistsRunningResultAndStartsExecutor() {
        StressTestConfig config = config();
        MetricsCollector.TestMetrics metrics = new MetricsCollector().create(UUID.randomUUID(), java.time.Instant.now());
        when(metricsCollector.create(any(), any())).thenReturn(metrics);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(workloadExecutor.start(any(), eq(config), eq(metrics), any(), any())).thenReturn(runWithPendingCompletion());

        UUID id = service.start(config);

        ArgumentCaptor<StressTestResult> captor = ArgumentCaptor.forClass(StressTestResult.class);
        verify(repository).save(captor.capture());
        assertThat(id).isNotNull();
        assertThat(captor.getValue().getStatus()).isEqualTo(TestStatus.RUNNING);
        assertThat(captor.getValue().getTargetDbUrl()).isEqualTo(config.getJdbcUrl());
    }

    @Test
    void stopSignalsRunningTest() {
        CountDownLatch stopSignal = new CountDownLatch(1);
        StressTestRun run = new StressTestRun(stopSignal, new AtomicInteger(), new AtomicReference<>(), new CompletableFuture<>());
        MetricsCollector.TestMetrics metrics = new MetricsCollector().create(UUID.randomUUID(), java.time.Instant.now());
        AtomicReference<StressTestResult> stored = new AtomicReference<>();
        when(metricsCollector.create(any(), any())).thenReturn(metrics);
        when(repository.save(any())).thenAnswer(invocation -> {
            StressTestResult saved = invocation.getArgument(0);
            stored.set(saved);
            return saved;
        });
        when(workloadExecutor.start(any(), any(), any(), any(), any())).thenReturn(run);

        UUID id = service.start(config());
        when(repository.findById(id)).thenReturn(Optional.of(stored.get()));
        service.stop(id);

        assertThat(stopSignal.getCount()).isZero();
    }

    @Test
    void rejectsRampUpLongerThanDuration() {
        StressTestConfig config = config();
        config.setRampUpSeconds(99);

        assertThatThrownBy(() -> service.start(config))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ramp-up");
    }

    private StressTestRun runWithPendingCompletion() {
        return new StressTestRun(new CountDownLatch(1), new AtomicInteger(), new AtomicReference<>(), new CompletableFuture<>());
    }

    private StressTestConfig config() {
        StressTestConfig config = new StressTestConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
        config.setUsername("user");
        config.setPassword("pass");
        config.setConcurrentThreads(5);
        config.setDurationSeconds(10);
        config.setRampUpSeconds(1);
        config.setWorkloadType(WorkloadType.MIXED);
        return config;
    }

    private StressTestResult result(UUID id, TestStatus status) {
        StressTestResult result = StressTestResult.fromConfig(id, config());
        result.setStatus(status);
        result.applyFinalMetrics(10, 0, 1.0, 2.0, new LatencyStats(1, 2, 3));
        return result;
    }
}
