package com.zebacodes.dbstress.service.workload;

import com.zebacodes.dbstress.service.dialect.DatabaseDialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

class MvccConcurrencyWorkload extends AbstractSqlWorkload {

    MvccConcurrencyWorkload(DatabaseDialect dialect) {
        super(dialect);
    }

    @Override
    public void execute(Connection connection, Random random) throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            executeFilteredReadFrom(connection, 1L + random.nextInt(100));
            executeWithValue(connection, dialect.updateSql(), value(random));
            executeFilteredReadFrom(connection, 1L + random.nextInt(100));
            if (random.nextInt(100) < 20) {
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
