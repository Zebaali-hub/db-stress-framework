package com.zebacodes.dbstress.service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zebacodes.dbstress.model.StressTestConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TargetDataSourceFactory {

    private final long maxLifetimeMs;
    private final long connectionTimeoutMs;
    private final long validationTimeoutMs;

    public TargetDataSourceFactory(
            @Value("${dbstress.target.pool.max-lifetime-ms:1800000}") long maxLifetimeMs,
            @Value("${dbstress.target.pool.connection-timeout-ms:10000}") long connectionTimeoutMs,
            @Value("${dbstress.target.pool.validation-timeout-ms:5000}") long validationTimeoutMs) {
        this.maxLifetimeMs = maxLifetimeMs;
        this.connectionTimeoutMs = connectionTimeoutMs;
        this.validationTimeoutMs = validationTimeoutMs;
    }

    public HikariDataSource create(UUID testId, StressTestConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("target-db-" + testId);
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setMaximumPoolSize(Math.max(1, config.getConcurrentThreads()));
        hikariConfig.setMinimumIdle(Math.min(2, Math.max(1, config.getConcurrentThreads())));
        hikariConfig.setMaxLifetime(maxLifetimeMs);
        hikariConfig.setConnectionTimeout(connectionTimeoutMs);
        hikariConfig.setValidationTimeout(validationTimeoutMs);
        hikariConfig.setAutoCommit(true);
        return new HikariDataSource(hikariConfig);
    }
}
