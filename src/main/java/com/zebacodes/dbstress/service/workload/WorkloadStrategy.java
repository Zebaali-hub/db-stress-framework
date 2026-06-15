package com.zebacodes.dbstress.service.workload;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

public interface WorkloadStrategy {

    void execute(Connection connection, Random random) throws SQLException;
}
