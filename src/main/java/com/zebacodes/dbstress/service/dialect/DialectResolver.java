package com.zebacodes.dbstress.service.dialect;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DialectResolver {

    private final List<DatabaseDialect> dialects;

    public DialectResolver(List<DatabaseDialect> dialects) {
        this.dialects = dialects;
    }

    public DatabaseDialect resolve(String jdbcUrl) {
        return dialects.stream()
                .filter(dialect -> dialect.supports(jdbcUrl))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported JDBC URL for default workloads: " + jdbcUrl));
    }
}
