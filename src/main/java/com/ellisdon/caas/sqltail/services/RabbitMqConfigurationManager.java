package com.ellisdon.caas.sqltail.services;

import com.ellisdon.caas.sqltail.clients.RabbitMqClient;
import com.ellisdon.caas.sqltail.domain.ExchangeConfiguration;
import com.ellisdon.caas.sqltail.repositories.ExchangeConfigurationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for auto binding exchanges and queues based on new configurations
 */
@Component
@EnableAsync
@Slf4j
public class RabbitMqConfigurationManager {

    public static final long FIVE_SECONDS = 5000;

    public static final String UNDERSCORE = "_";

    @Autowired
    RabbitMqClient rabbitMqClient;

    @Autowired
    ExchangeConfigurationProvider exchangeConfigurationProvider;

    @Autowired
    ExchangeConfigurationRepository exchangeConfigurationRepository;

    @Scheduled(initialDelay = 0, fixedRate = FIVE_SECONDS)
    public void refreshExchangeBindingConfiguration() {
        getLatestExchangeConfig();
    }

    @Async
    void getLatestExchangeConfig() {
        Map<String, List<String>> exchangeConfigurationMap = new HashMap<>();

        Iterable<ExchangeConfiguration> latestConfigurations = exchangeConfigurationRepository.findAll();
        for (ExchangeConfiguration configuration : latestConfigurations) {
            String exchangeName = configuration.getFeature();
            List<String> tableNames = configuration.getTableNames();
            tableNames.forEach(tableName -> {
                String queueName = exchangeName + UNDERSCORE + tableName;
                rabbitMqClient.bindToKey(exchangeName, tableName, queueName);
            });

            exchangeConfigurationMap.put(exchangeName, tableNames);
        }

        log.info("Refreshed exchange configuration with {}", exchangeConfigurationMap);
        exchangeConfigurationProvider.updateExchangeConfiguration(exchangeConfigurationMap);
    }
}

