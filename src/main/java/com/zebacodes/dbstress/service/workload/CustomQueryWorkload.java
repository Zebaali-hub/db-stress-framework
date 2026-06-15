package com.zebacodes.dbstress.service.workload;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

class CustomQueryWorkload implements WorkloadStrategy {

    private final List<String> queries;

    CustomQueryWorkload(List<String> queries) {
        this.queries = queries.stream()
                .filter(query -> query != null && !query.isBlank())
                .map(String::trim)
                .toList();
    }

    @Override
    public void execute(Connection connection, Random random) throws SQLException {
        String sql = queries.get(random.nextInt(queries.size()));
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }
}
