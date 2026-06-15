package com.zebacodes.dbstress.service.workload;

import com.zebacodes.dbstress.service.dialect.DatabaseDialect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

abstract class AbstractSqlWorkload implements WorkloadStrategy {

    protected final DatabaseDialect dialect;

    protected AbstractSqlWorkload(DatabaseDialect dialect) {
        this.dialect = dialect;
    }

    protected void executeSimple(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }

    protected void executeWithValue(Connection connection, String sql, String value) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            statement.executeUpdate();
        }
    }

    protected void executeFilteredRead(Connection connection, Random random) throws SQLException {
        executeFilteredReadFrom(connection, Math.max(1, random.nextInt(10_000)));
    }

    protected void executeFilteredReadFrom(Connection connection, long lowerBoundId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(dialect.filteredReadSql())) {
            statement.setLong(1, Math.max(1, lowerBoundId));
            statement.execute();
        }
    }

    protected String value(Random random) {
        return "stress-" + System.nanoTime() + "-" + random.nextInt(1_000_000);
    }
}
