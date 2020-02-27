package com.ellisdon.caas.sqltail.components;

import com.ellisdon.caas.sqltail.domain.RowValueDetails;
import com.ellisdon.caas.sqltail.services.MessageProducer;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SqlTailEventListener implements BinaryLogClient.EventListener {

    Map<Long, String> tableNameMap = new HashMap<>();

    Map<String, Map<String, String>> tableInfo = new HashMap<>();

    @Autowired
    Gson gson;

    @Autowired
    MessageProducer messageProducer;

    public void setTableInfo(Map<String, Map<String, String>> tableInfo) {
        this.tableInfo = tableInfo;
    }

    @Override
    public void onEvent(Event event) {
        EventType eventType = event.getHeader().getEventType();
        switch (eventType) {
            case TABLE_MAP:
                TableMapEventData tableMapEventData = event.getData();
                // handle tableMapEventData (contains mapping between *RowsEventData::tableId and table name
                // that can be used (together with https://github.com/shyiko/mysql-binlog-connector-java/issues/24#issuecomment-43747417)
                // to map column names to the values)
                tableNameMap.put(tableMapEventData.getTableId(), tableMapEventData.getTable());
                break;
            case PRE_GA_WRITE_ROWS:
            case WRITE_ROWS:
            case EXT_WRITE_ROWS:
                // handle writeRowsEventData (generated when someone INSERTs data)
                WriteRowsEventData writeRowsEventData = event.getData();
                for (Serializable[] row : writeRowsEventData.getRows()) {
                    String tableName = tableNameMap.get(writeRowsEventData.getTableId());
                    log.info("Inserting into {} with {}", tableName, handleSerializableRow(row).toString());
                    if (tableInfo.get(tableName) != null) {
                        messageProducer.sendMessage(tableName, assembleMessage(row, tableName));
                    }
                }
                break;
            case PRE_GA_UPDATE_ROWS:
            case UPDATE_ROWS:
            case EXT_UPDATE_ROWS:
                // handle updateRowsEventData (generated when someone UPDATEs data)
                UpdateRowsEventData updateRowsEventData = event.getData();
                for (Map.Entry<Serializable[], Serializable[]> row : updateRowsEventData.getRows()) {
                    String tableName = tableNameMap.get(updateRowsEventData.getTableId());
                    log.info("Updating {} with {}", tableNameMap.get(updateRowsEventData.getTableId()), handleSerializableRow(row.getValue()).toString());
                    if (tableInfo.get(tableName) != null) {
                        messageProducer.sendMessage(tableName, assembleMessage(row.getValue(), tableName));
                    }
                }
                break;
            case PRE_GA_DELETE_ROWS:
            case DELETE_ROWS:
            case EXT_DELETE_ROWS:
                // handle deleteRowsEventData (generated when someone DELETEs data)
                DeleteRowsEventData deleteRowsEventData = event.getData();
                for (Serializable[] row : deleteRowsEventData.getRows()) {
                    String tableName = tableNameMap.get(deleteRowsEventData.getTableId());
                    log.info("Deleting from {} with {}", tableNameMap.get(deleteRowsEventData.getTableId()), handleSerializableRow(row).toString());
                    if (tableInfo.get(tableName) != null) {
                        messageProducer.sendMessage(tableName, assembleMessage(row, tableName));
                    }
                }
                break;
        }
    }

    private String assembleMessage(Serializable[] row, String tableName) {
        Map<String, RowValueDetails> messageAsMap = new HashMap<>();
        Map<String, String> tableColumnInfo = tableInfo.get(tableName);
        for (int i = 0; i < tableColumnInfo.keySet().size(); i++) {
            String column = (String) tableColumnInfo.keySet().toArray()[i];
            messageAsMap.put(column, new RowValueDetails(tableColumnInfo.get(column), serializableToString(row[i])));
        }

        log.debug(gson.toJson(messageAsMap));
        return gson.toJson(messageAsMap);

    }

    private List<String> handleSerializableRow(Serializable[] row) {
        return Arrays.stream(row).map(c -> {
                    String value = null;
                    if (c instanceof byte[]) {
                        value = new String((byte[]) c, StandardCharsets.UTF_8);
                    } else if (c != null) {
                        value = String.valueOf(c);
                    }
                    return value;
                }
        ).collect(Collectors.toList());
    }

    private String serializableToString(Serializable serializable) {
        if (serializable instanceof byte[]) {
            return new String((byte[]) serializable, StandardCharsets.UTF_8);
        } else if (serializable != null) {
            return String.valueOf(serializable);
        }
        return null;
    }
}
