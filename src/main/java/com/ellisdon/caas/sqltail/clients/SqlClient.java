package com.ellisdon.caas.sqltail.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SqlClient {

    public static final String SELECT_TABLE_NAME_COLUMN_NAME_DATA_TYPE_FROM_INFORMATION_SCHEMA_COLUMNS = "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE from INFORMATION_SCHEMA.COLUMNS;";

    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> queryDbInformation() {
        return jdbcTemplate.queryForList(SELECT_TABLE_NAME_COLUMN_NAME_DATA_TYPE_FROM_INFORMATION_SCHEMA_COLUMNS);
    }

}
