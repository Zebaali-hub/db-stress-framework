package com.zebacodes.dbstress.service.workload;

import com.zebacodes.dbstress.model.StressTestConfig;
import com.zebacodes.dbstress.service.dialect.DatabaseDialect;
import org.springframework.stereotype.Component;

@Component
public class WorkloadStrategyFactory {

    public WorkloadStrategy create(StressTestConfig config, DatabaseDialect dialect) {
        if (config.hasCustomQueries()) {
            return new CustomQueryWorkload(config.getCustomQueries());
        }
        return switch (config.getWorkloadType()) {
            case READ_HEAVY -> new ReadHeavyWorkload(dialect);
            case WRITE_HEAVY -> new WriteHeavyWorkload(dialect);
            case MIXED -> new MixedWorkload(dialect);
            case TRANSACTION_HEAVY -> new TransactionHeavyWorkload(dialect);
            case FEATURE_PARTITIONING -> new PartitioningWorkload(dialect);
            case FEATURE_INDEXING -> new IndexingWorkload(dialect);
            case FEATURE_CACHE_CONTENTION -> new CacheContentionWorkload(dialect);
            case FEATURE_MVCC_CONCURRENCY -> new MvccConcurrencyWorkload(dialect);
            case FEATURE_QUERY_OPTIMIZER -> new QueryOptimizerWorkload(dialect);
            case FEATURE_VECTOR_SEARCH -> new VectorSearchWorkload(dialect);
            case TPC_C_LIKE -> new TpcCLikeWorkload(dialect);
            case TPC_H_LIKE -> new TpcHLikeWorkload(dialect);
        };
    }
}
