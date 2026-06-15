package com.zebacodes.dbstress.service.workload;

import com.zebacodes.dbstress.service.dialect.DatabaseDialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

class CacheContentionWorkload extends AbstractSqlWorkload {

    CacheContentionWorkload(DatabaseDialect dialect) {
        super(dialect);
    }

    @Override
    public void execute(Connection connection, Random random) throws SQLException {
        int roll = random.nextInt(100);
        if (roll < 75) {
            executeFilteredReadFrom(connection, 1L + random.nextInt(25));
        } else {
            executeWithValue(connection, dialect.updateSql(), "hot-block-" + random.nextInt(25));
        }
    }
}
