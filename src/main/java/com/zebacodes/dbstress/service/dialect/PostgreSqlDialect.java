package com.zebacodes.dbstress.service.dialect;

import org.springframework.stereotype.Component;

@Component
public class PostgreSqlDialect implements DatabaseDialect {

    @Override
    public String name() {
        return "PostgreSQL";
    }

    @Override
    public boolean supports(String jdbcUrl) {
        return jdbcUrl != null && jdbcUrl.startsWith("jdbc:postgresql:");
    }

    @Override
    public String createWorkloadTableSql() {
        return """
                CREATE TABLE IF NOT EXISTS stress_test_data (
                    id SERIAL PRIMARY KEY,
                    value TEXT NOT NULL,
                    updated_at TIMESTAMP DEFAULT NOW()
                )
                """;
    }

    @Override
    public String simpleReadSql() {
        return "SELECT 1";
    }

    @Override
    public String filteredReadSql() {
        return "SELECT id, value FROM stress_test_data WHERE id >= ? ORDER BY id LIMIT 25";
    }

    @Override
    public String aggregateReadSql() {
        return "SELECT count(*), max(updated_at) FROM stress_test_data";
    }

    @Override
    public String insertSql() {
        return "INSERT INTO stress_test_data (value, updated_at) VALUES (?, NOW())";
    }

    @Override
    public String updateSql() {
        return "UPDATE stress_test_data SET value = ?, updated_at = NOW() WHERE id IN (SELECT id FROM stress_test_data ORDER BY id DESC LIMIT 10)";
    }

    @Override
    public String deleteSql() {
        return "DELETE FROM stress_test_data WHERE id IN (SELECT id FROM stress_test_data ORDER BY id ASC LIMIT 1)";
    }
}
