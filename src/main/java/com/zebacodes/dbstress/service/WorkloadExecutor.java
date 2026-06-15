package com.zebacodes.dbstress.service;

import com.zaxxer.hikari.HikariDataSource;
import com.zebacodes.dbstress.model.LiveMetrics;
import com.zebacodes.dbstress.model.StressTestConfig;
import com.zebacodes.dbstress.model.TestStatus;
import com.zebacodes.dbstress.service.dialect.DatabaseDialect;
import com.zebacodes.dbstress.service.dialect.DialectResolver;
import com.zebacodes.dbstress.service.execution.ExecutionSummary;
import com.zebacodes.dbstress.service.execution.StressTestRun;
import com.zebacodes.dbstress.service.workload.WorkloadStrategy;
import com.zebacodes.dbstress.service.workload.WorkloadStrategyFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
public class WorkloadExecutor {

    private final TargetDataSourceFactory dataSourceFactory;
    private final DialectResolver dialectResolver;
    private final WorkloadStrategyFactory workloadStrategyFactory;

    public WorkloadExecutor(TargetDataSourceFactory dataSourceFactory,
                            DialectResolver dialectResolver,
                            WorkloadStrategyFactory workloadStrategyFactory) {
        this.dataSourceFactory = dataSourceFactory;
        this.dialectResolver = dialectResolver;
        this.workloadStrategyFactory = workloadStrategyFactory;
    }

    public StressTestRun start(UUID testId,
                               StressTestConfig config,
                               MetricsCollector.TestMetrics metrics,
                               Supplier<TestStatus> statusSupplier,
                               Consumer<LiveMetrics> metricsConsumer) {
        CountDownLatch stopSignal = new CountDownLatch(1);
        AtomicInteger activeThreads = new AtomicInteger();
        AtomicReference<String> failureMessage = new AtomicReference<>();
        CompletableFuture<ExecutionSummary> completion = new CompletableFuture<>();

        Thread coordinator = new Thread(() -> {
            ExecutorService workers = Executors.newFixedThreadPool(config.getConcurrentThreads());
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            try (HikariDataSource dataSource = dataSourceFactory.create(testId, config)) {
                DatabaseDialect dialect = config.hasCustomQueries() ? null : dialectResolver.resolve(config.getJdbcUrl());
                if (dialect != null) {
                    initializeTargetSchema(dataSource, dialect);
                }
                WorkloadStrategy strategy = workloadStrategyFactory.create(config, dialect);
                Instant deadline = Instant.now().plusSeconds(config.getDurationSeconds());

                scheduler.scheduleAtFixedRate(() -> {
                    LiveMetrics snapshot = metrics.snapshot(activeThreads.get(), statusSupplier.get());
                    metricsConsumer.accept(snapshot);
                }, 1, 1, TimeUnit.SECONDS);

                rampWorkers(config, workers, strategy, dataSource, metrics, activeThreads, stopSignal, deadline, failureMessage);
                awaitDeadlineOrStop(stopSignal, deadline);

                TestStatus finalStatus = stopSignal.getCount() == 0 ? TestStatus.STOPPED : TestStatus.COMPLETED;
                shutdownWorkers(workers);
                scheduler.shutdownNow();
                LiveMetrics finalSnapshot = metrics.snapshot(activeThreads.get(), finalStatus);
                metricsConsumer.accept(finalSnapshot);
                completion.complete(summary(metrics, finalStatus, null));
            } catch (Exception e) {
                failureMessage.set(e.getMessage());
                scheduler.shutdownNow();
                workers.shutdownNow();
                completion.complete(summary(metrics, TestStatus.FAILED, e.getMessage()));
            }
        }, "stress-coordinator-" + testId);

        coordinator.start();
        return new StressTestRun(stopSignal, activeThreads, failureMessage, completion);
    }

    private void initializeTargetSchema(HikariDataSource dataSource, DatabaseDialect dialect) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(dialect.createWorkloadTableSql());
        }
    }

    private void rampWorkers(StressTestConfig config,
                             ExecutorService workers,
                             WorkloadStrategy strategy,
                             HikariDataSource dataSource,
                             MetricsCollector.TestMetrics metrics,
                             AtomicInteger activeThreads,
                             CountDownLatch stopSignal,
                             Instant deadline,
                             AtomicReference<String> failureMessage) throws InterruptedException {
        long intervalMillis = config.getRampUpSeconds() <= 0
                ? 0
                : Math.max(1, Duration.ofSeconds(config.getRampUpSeconds()).toMillis() / config.getConcurrentThreads());

        for (int i = 0; i < config.getConcurrentThreads(); i++) {
            if (stopSignal.getCount() == 0 || Instant.now().isAfter(deadline)) {
                break;
            }
            workers.submit(() -> workerLoop(strategy, dataSource, metrics, activeThreads, stopSignal, deadline, failureMessage));
            if (intervalMillis > 0 && i < config.getConcurrentThreads() - 1) {
                boolean stopped = stopSignal.await(intervalMillis, TimeUnit.MILLISECONDS);
                if (stopped) {
                    break;
                }
            }
        }
    }

    private void workerLoop(WorkloadStrategy strategy,
                            HikariDataSource dataSource,
                            MetricsCollector.TestMetrics metrics,
                            AtomicInteger activeThreads,
                            CountDownLatch stopSignal,
                            Instant deadline,
                            AtomicReference<String> failureMessage) {
        activeThreads.incrementAndGet();
        Random random = new Random();
        try {
            while (stopSignal.getCount() > 0 && Instant.now().isBefore(deadline)) {
                long startNanos = System.nanoTime();
                try (Connection connection = dataSource.getConnection()) {
                    strategy.execute(connection, random);
                    double latencyMs = (System.nanoTime() - startNanos) / 1_000_000.0;
                    metrics.recordLatency(latencyMs);
                    metrics.incrementTransactions();
                } catch (SQLException e) {
                    metrics.incrementErrors();
                    failureMessage.compareAndSet(null, e.getMessage());
                }
            }
        } finally {
            activeThreads.decrementAndGet();
        }
    }

    private void awaitDeadlineOrStop(CountDownLatch stopSignal, Instant deadline) throws InterruptedException {
        while (stopSignal.getCount() > 0 && Instant.now().isBefore(deadline)) {
            long remainingMillis = Math.max(1, Duration.between(Instant.now(), deadline).toMillis());
            stopSignal.await(Math.min(remainingMillis, 250), TimeUnit.MILLISECONDS);
        }
    }

    private void shutdownWorkers(ExecutorService workers) throws InterruptedException {
        workers.shutdown();
        if (!workers.awaitTermination(30, TimeUnit.SECONDS)) {
            workers.shutdownNow();
        }
    }

    private ExecutionSummary summary(MetricsCollector.TestMetrics metrics, TestStatus status, String failureMessage) {
        return new ExecutionSummary(
                status,
                metrics.getTotalTransactions(),
                metrics.getTotalErrors(),
                metrics.averageTps(),
                metrics.getPeakTps(),
                metrics.finalLatencyStats(),
                failureMessage
        );
    }
}
