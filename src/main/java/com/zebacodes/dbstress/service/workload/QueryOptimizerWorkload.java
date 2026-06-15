package com.zebacodes.dbstress.service.workload;

import com.zebacodes.dbstress.service.dialect.DatabaseDialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

class QueryOptimizerWorkload extends AbstractSqlWorkload {

    QueryOptimizerWorkload(DatabaseDialect dialect) {
        super(dialect);
    }

    @Override
    public void execute(Connection connection, Random random) throws SQLException {
        int roll = random.nextInt(100);
        if (roll < 45) {
            executeSimple(connection, dialect.aggregateReadSql());
        } else if (roll < 85) {
            executeFilteredRead(connection, random);
        } else {
            executeSimple(connection, dialect.simpleReadSql());
        }
    }
}
