package com.ellisdon.caas.sqltail.services;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;


@Service
@Slf4j
public class SqlTailService {

    public static final String SELECT_TABLE_NAME_COLUMN_NAME_DATA_TYPE_FROM_INFORMATION_SCHEMA_COLUMNS = "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE from INFORMATION_SCHEMA.COLUMNS;";

    public static final String TABLE_NAME = "TABLE_NAME";

    public static final String COLUMN_NAME = "COLUMN_NAME";

    public static final String DATA_TYPE = "DATA_TYPE";

    public static final int PORT = 3306;

    @Value("${mysqlhost}")
    private String host;

    @Value("${mysqlSchema}")
    private String schema;

    @Value("${mysqlUsername}")
    private String username;

    @Value("${mysqlPassword}")
    private String password;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    SqlTailEventListener eventListener;

    public void startSqlLogListener() throws IOException {
        checkNotNull(host, "Database Url was not specified!");
        checkNotNull(schema, "Database schema was not specified!");
        checkNotNull(username, "Database username was not specified!");
        checkNotNull(password, "Database password was not specified!");

        log.info("Attempting to connect to {}", host);
        BinaryLogClient client = new BinaryLogClient(host, PORT, schema, username, password);
        EventDeserializer eventDeserializer = new EventDeserializer();

        eventDeserializer.setCompatibilityMode(
                EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG,
                EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY
        );
        client.setEventDeserializer(eventDeserializer);
        client.registerEventListener(eventListener);

        List<Map<String, Object>> schemaInfo = jdbcTemplate.queryForList(SELECT_TABLE_NAME_COLUMN_NAME_DATA_TYPE_FROM_INFORMATION_SCHEMA_COLUMNS);
        Map<String, Map<String, String>> tableInfo = constructTableInfo(schemaInfo);  // Map<TableName<Map<ColumnName, ColumnDataType>>
        eventListener.setTableInfo(tableInfo);

        try {
            client.connect();
        } catch (IOException e) {
            log.error("Failed to connect!", e);
            throw e;
        }
    }

    private Map<String, Map<String, String>> constructTableInfo(List<Map<String, Object>> schemaInfo) {
        Map<String, Map<String, String>> tableInfo = new HashMap<>();
        for (Map<String, Object> info : schemaInfo) {
            String tableName = (String) info.get(TABLE_NAME);
            String columnName = (String) info.get(COLUMN_NAME);
            String dataType = (String) info.get(DATA_TYPE);

            if (tableInfo.get(tableName) == null) {
                Map<String, String> tableColumnMap = new HashMap<>();
                tableColumnMap.put(columnName, dataType);
                tableInfo.put(tableName, tableColumnMap);
            } else {
                Map<String, String> columnInfoMap = tableInfo.get(tableName);
                columnInfoMap.put(columnName, dataType);
                tableInfo.put(tableName, columnInfoMap);
            }
        }
        return tableInfo;
    }
}
