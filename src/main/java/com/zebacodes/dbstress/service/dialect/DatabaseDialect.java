package com.zebacodes.dbstress.service.dialect;

public interface DatabaseDialect {

    String name();

    boolean supports(String jdbcUrl);

    String createWorkloadTableSql();

    String simpleReadSql();

    String filteredReadSql();

    String aggregateReadSql();

    String insertSql();

    String updateSql();

    String deleteSql();
}
