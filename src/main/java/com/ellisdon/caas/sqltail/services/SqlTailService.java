package com.ellisdon.caas.sqltail.services;

import com.ellisdon.caas.sqltail.clients.BinLogClientFactory;
import com.ellisdon.caas.sqltail.clients.SqlClient;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SqlTailService {

    private static final String TABLE_NAME = "TABLE_NAME";

    private static final String COLUMN_NAME = "COLUMN_NAME";

    private static final String DATA_TYPE = "DATA_TYPE";

    @Autowired
    SqlTailEventListener eventListener;

    @Autowired
    BinLogClientFactory binLogClientFactory;

    @Autowired
    SqlClient sqlClient;

    @EventListener(ApplicationReadyEvent.class)
    public void startSqlTail() throws IOException {
        try {
            List<Map<String, Object>> schemaInfo = sqlClient.queryDbInformation();
            Map<String, Map<String, String>> tableInfo = constructTableInfo(schemaInfo);  // Map<TableName<Map<ColumnName, ColumnDataType>>
            eventListener.setTableInfo(tableInfo);

            BinaryLogClient binaryLogClient = binLogClientFactory.getBinaryLogClient();
            binaryLogClient.registerEventListener(eventListener);
            binaryLogClient.connect();

        } catch (IOException e) {
            log.error("Failed to connect to SQL DB!", e);
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
