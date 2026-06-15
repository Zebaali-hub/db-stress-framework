package com.zebacodes.dbstress.service.workload;

import com.zebacodes.dbstress.service.dialect.DatabaseDialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

class MixedWorkload extends AbstractSqlWorkload {

    MixedWorkload(DatabaseDialect dialect) {
        super(dialect);
    }

    @Override
    public void execute(Connection connection, Random random) throws SQLException {
        int roll = random.nextInt(100);
        if (roll < 50) {
            executeFilteredRead(connection, random);
        } else if (roll < 80) {
            executeWithValue(connection, dialect.insertSql(), value(random));
        } else {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                executeWithValue(connection, dialect.insertSql(), value(random));
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
}
