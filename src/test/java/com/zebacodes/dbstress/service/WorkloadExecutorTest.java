package com.zebacodes.dbstress.service;

import com.zebacodes.dbstress.model.StressTestConfig;
import com.zebacodes.dbstress.model.TestStatus;
import com.zebacodes.dbstress.model.WorkloadType;
import com.zebacodes.dbstress.service.dialect.DialectResolver;
import com.zebacodes.dbstress.service.dialect.MySqlDialect;
import com.zebacodes.dbstress.service.dialect.OracleDialect;
import com.zebacodes.dbstress.service.dialect.PostgreSqlDialect;
import com.zebacodes.dbstress.service.execution.ExecutionSummary;
import com.zebacodes.dbstress.service.execution.StressTestRun;
import com.zebacodes.dbstress.service.workload.WorkloadStrategyFactory;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class WorkloadExecutorTest {

    @Test
    void executesCustomQueriesAndCompletes() throws Exception {
        WorkloadExecutor executor = executor();
        UUID id = UUID.randomUUID();
        MetricsCollector.TestMetrics metrics = new MetricsCollector().create(id, Instant.now());
        AtomicInteger snapshots = new AtomicInteger();

        StressTestRun run = executor.start(id, config(), metrics, () -> TestStatus.RUNNING, ignored -> snapshots.incrementAndGet());
        ExecutionSummary summary = run.completion().get(5, TimeUnit.SECONDS);

        assertThat(summary.status()).isEqualTo(TestStatus.COMPLETED);
        assertThat(summary.totalTransactions()).isGreaterThan(0);
        assertThat(snapshots.get()).isGreaterThan(0);
    }

    @Test
    void stopSignalEndsRunningTest() throws Exception {
        WorkloadExecutor executor = executor();
        UUID id = UUID.randomUUID();
        MetricsCollector.TestMetrics metrics = new MetricsCollector().create(id, Instant.now());
        StressTestConfig config = config();
        config.setDurationSeconds(5);

        StressTestRun run = executor.start(id, config, metrics, () -> TestStatus.RUNNING, ignored -> { });
        Thread.sleep(200);
        run.stop();
        ExecutionSummary summary = run.completion().get(5, TimeUnit.SECONDS);

        assertThat(summary.status()).isEqualTo(TestStatus.STOPPED);
    }

    private WorkloadExecutor executor() {
        TargetDataSourceFactory dataSourceFactory = new TargetDataSourceFactory(1_800_000, 10_000, 5_000);
        DialectResolver dialectResolver = new DialectResolver(List.of(new PostgreSqlDialect(), new MySqlDialect(), new OracleDialect()));
        return new WorkloadExecutor(dataSourceFactory, dialectResolver, new WorkloadStrategyFactory());
    }

    private StressTestConfig config() {
        StressTestConfig config = new StressTestConfig();
        config.setJdbcUrl("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE");
        config.setUsername("sa");
        config.setPassword("");
        config.setConcurrentThreads(2);
        config.setDurationSeconds(1);
        config.setRampUpSeconds(0);
        config.setWorkloadType(WorkloadType.READ_HEAVY);
        config.setCustomQueries(List.of("SELECT 1"));
        return config;
    }
}
