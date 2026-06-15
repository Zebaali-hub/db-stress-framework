package com.zebacodes.dbstress.service.execution;

import com.zebacodes.dbstress.model.LatencyStats;
import com.zebacodes.dbstress.model.TestStatus;

public record ExecutionSummary(
        TestStatus status,
        long totalTransactions,
        long totalErrors,
        double averageTps,
        double peakTps,
        LatencyStats latencyStats,
        String failureMessage
) {
}
