package com.ellisdon.caas.sqltail.services;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class SqlTailService {

    @Autowired
    SqlTailEventListener eventListener;

    @Value("${database.sql.host}")
    private String host;

    @Value("${database.sql.schema}")
    private String schema;

    @Value("${database.sql.port}")
    private String port;

    @Value("${database.sql.username}")
    private String username;

    @Value("${database.sql.password}")
    private String password;

    public void startSqlLogListener() throws IOException {
        log.info("Attempting to connect to {}", host);
        BinaryLogClient client = new BinaryLogClient(host, Integer.parseInt(port), schema, username, password);
        EventDeserializer eventDeserializer = new EventDeserializer();

        eventDeserializer.setCompatibilityMode(
                EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG,
                EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY
        );
        client.setEventDeserializer(eventDeserializer);
        client.registerEventListener(eventListener);

        try {
            client.connect();
        } catch (IOException e) {
            log.error("Failed to connect!", e);
            throw e;
        }
    }


}
