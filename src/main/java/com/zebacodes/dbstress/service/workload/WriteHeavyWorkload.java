package com.zebacodes.dbstress.service.workload;

import com.zebacodes.dbstress.service.dialect.DatabaseDialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

class WriteHeavyWorkload extends AbstractSqlWorkload {

    WriteHeavyWorkload(DatabaseDialect dialect) {
        super(dialect);
    }

    @Override
    public void execute(Connection connection, Random random) throws SQLException {
        int roll = random.nextInt(100);
        if (roll < 60) {
            executeWithValue(connection, dialect.insertSql(), value(random));
        } else if (roll < 90) {
            executeWithValue(connection, dialect.updateSql(), value(random));
        } else {
            executeSimple(connection, dialect.deleteSql());
        }
    }
}
