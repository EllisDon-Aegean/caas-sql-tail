package com.ellisdon.caas.sqltail.services;

import com.ellisdon.caas.sqltail.clients.RabbitMqClient;
import com.ellisdon.caas.sqltail.components.ExchangeConfigurationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class MessageProducer {

    @Autowired
    RabbitMqClient rabbitMqClient;

    @Autowired
    ExchangeConfigurationProvider exchangeConfigurationProvider;

    public void sendMessage(String tableName, String jsonMessage) {
        List<String> exchanges = exchangeConfigurationProvider.getExchangesUsingTableName(tableName);

        exchanges.forEach(exchangeName -> {
            String messageId = UUID.randomUUID().toString();

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setMessageId(messageId);
            messageProperties.setContentEncoding(MessageProperties.CONTENT_TYPE_JSON);
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);

            Message rabbitMqMessage = new Message(jsonMessage.getBytes(), messageProperties);
            rabbitMqClient.sendMessage(exchangeName, tableName, rabbitMqMessage);
            log.info("Sent message with id {} on exchange {} with routing key {}", messageId, exchangeName, tableName);
        });
    }

}
