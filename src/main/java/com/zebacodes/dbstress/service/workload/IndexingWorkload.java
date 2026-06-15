package com.zebacodes.dbstress.service.workload;

import com.zebacodes.dbstress.service.dialect.DatabaseDialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

class IndexingWorkload extends AbstractSqlWorkload {

    IndexingWorkload(DatabaseDialect dialect) {
        super(dialect);
    }

    @Override
    public void execute(Connection connection, Random random) throws SQLException {
        int roll = random.nextInt(100);
        if (roll < 65) {
            executeFilteredRead(connection, random);
        } else if (roll < 85) {
            executeWithValue(connection, dialect.updateSql(), value(random));
        } else {
            executeWithValue(connection, dialect.insertSql(), value(random));
        }
    }
}
