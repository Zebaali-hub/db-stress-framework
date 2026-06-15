package com.zebacodes.dbstress.service.workload;

import com.zebacodes.dbstress.service.dialect.DatabaseDialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

class TransactionHeavyWorkload extends AbstractSqlWorkload {

    TransactionHeavyWorkload(DatabaseDialect dialect) {
        super(dialect);
    }

    @Override
    public void execute(Connection connection, Random random) throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            executeWithValue(connection, dialect.insertSql(), value(random));
            executeWithValue(connection, dialect.updateSql(), value(random));
            executeSimple(connection, dialect.aggregateReadSql());
            if (random.nextInt(100) < 10) {
                connection.rollback();
            } else {
                connection.commit();
            }
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }
}
