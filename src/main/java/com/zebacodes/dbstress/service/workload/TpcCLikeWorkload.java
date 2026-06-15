package com.zebacodes.dbstress.service.workload;

import com.zebacodes.dbstress.service.dialect.DatabaseDialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

class TpcCLikeWorkload extends AbstractSqlWorkload {

    TpcCLikeWorkload(DatabaseDialect dialect) {
        super(dialect);
    }

    @Override
    public void execute(Connection connection, Random random) throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            executeWithValue(connection, dialect.insertSql(), "new-order-" + random.nextInt(1_000_000));
            executeWithValue(connection, dialect.updateSql(), "payment-" + random.nextInt(1_000_000));
            executeFilteredRead(connection, random);
            executeSimple(connection, dialect.aggregateReadSql());
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }
}
