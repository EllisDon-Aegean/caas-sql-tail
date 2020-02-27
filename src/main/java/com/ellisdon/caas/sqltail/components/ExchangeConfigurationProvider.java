package com.ellisdon.caas.sqltail.components;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExchangeConfigurationProvider {
    private Map<String, List<String>> exchangeConfigurationMap = new HashMap<>();  // Map<ExchangeName, List<TableName>>

    public void updateExchangeConfiguration(Map<String, List<String>> exchangeConfigurationMap) {
        this.exchangeConfigurationMap = exchangeConfigurationMap;
    }

    public List<String> getExchangesUsingTableName(String tableName) {
        List<String> exchanges = new ArrayList<>();
        exchangeConfigurationMap.forEach((exchangeName, tableNames) -> {
            if (tableNames.contains(tableName)) {
                exchanges.add(exchangeName);
            }
        });

        return exchanges;
    }
}
