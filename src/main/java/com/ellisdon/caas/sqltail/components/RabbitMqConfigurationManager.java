package com.ellisdon.caas.sqltail.components;

import com.ellisdon.caas.sqltail.clients.RabbitMqClient;
import com.ellisdon.caas.sqltail.domain.Feature;
import com.ellisdon.caas.sqltail.domain.Service;
import com.ellisdon.caas.sqltail.repositories.FeatureRepository;
import com.ellisdon.caas.sqltail.repositories.ServiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Responsible for auto binding exchanges and queues based on new configurations
 */
@Component
@EnableAsync
@Slf4j
public class RabbitMqConfigurationManager {

    public static final long FIVE_SECONDS = 5000;

    public static final String UNDERSCORE = "_";

    public static final String QUEUE = "queue";

    @Autowired
    RabbitMqClient rabbitMqClient;

    @Autowired
    ExchangeConfigurationProvider exchangeConfigurationProvider;

    @Autowired
    FeatureRepository featureRepository;

    @Autowired
    ServiceRepository serviceRepository;


    @Scheduled(initialDelay = 0, fixedRate = FIVE_SECONDS)
    public void refreshExchangeBindingConfiguration() {
        getLatestExchangeConfig();
    }

    @Async
    void getLatestExchangeConfig() {
        Map<String, List<String>> exchangeConfigurationMap = new HashMap<>();
        List<Feature> featureList = new ArrayList<>();
        featureRepository.findAll().forEach(featureList::add);

        Iterable<Service> services = serviceRepository.findAll();
        for (Service service : services) {
            String serviceName = service.getName();
            service.getFeatureIds().forEach(serviceFeatureId -> {
                Optional<Feature> optionalFeature = featureList.stream().filter(feature -> serviceFeatureId.equals(feature.getId())).findFirst();
                if (optionalFeature.isPresent()) {
                    Feature feature = optionalFeature.get();
                    String exchangeName = serviceName + UNDERSCORE + feature.getName();
                    feature.getTableNames().forEach(tableName -> {
                        String queueName = exchangeName + UNDERSCORE + tableName + UNDERSCORE + QUEUE;
                        rabbitMqClient.bindToKey(exchangeName, tableName, queueName);
                    });
                    exchangeConfigurationMap.put(exchangeName, feature.getTableNames());
                } else {
                    log.error("Unable to find feature with id {} for service {}", serviceFeatureId, serviceName);
                }
            });
        }

        log.info("Refreshed exchange configuration with {}", exchangeConfigurationMap);
        exchangeConfigurationProvider.updateExchangeConfiguration(exchangeConfigurationMap);
    }
}

