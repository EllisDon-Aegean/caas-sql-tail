package com.ellisdon.caas.sqltail.services;

import com.ellisdon.caas.sqltail.clients.RabbitMqClient;
import com.ellisdon.caas.sqltail.domain.Feature;
import com.ellisdon.caas.sqltail.repositories.FeatureRepository;
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
    FeatureRepository featureRepository;

    @Scheduled(initialDelay = 0, fixedRate = FIVE_SECONDS)
    public void refreshExchangeBindingConfiguration() {
        getLatestExchangeConfig();
    }

    @Async
    void getLatestExchangeConfig() {
        Map<String, List<String>> exchangeConfigurationMap = new HashMap<>();

        Iterable<Feature> features = featureRepository.findAll();
        for (Feature feature : features) {
            String exchangeName = feature.getFeature();
            List<String> tableNames = feature.getTableNames();
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

