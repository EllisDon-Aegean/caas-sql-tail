package com.ellisdon.caas.sqltail.clients;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BinLogClientFactory {
    public static final int PORT = 3306;

    @Value("${mysqlhost}")
    private String host;

    @Value("${mysqlSchema}")
    private String schema;

    @Value("${mysqlUsername}")
    private String username;

    @Value("${mysqlPassword}")
    private String password;

    BinaryLogClient client;

    public BinaryLogClient getBinaryLogClient() {
        if (client == null) {
            client = new BinaryLogClient(host, PORT, schema, username, password);

            EventDeserializer eventDeserializer = new EventDeserializer();
            eventDeserializer.setCompatibilityMode(
                    EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG,
                    EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY
            );
            client.setEventDeserializer(eventDeserializer);
        }

        return client;
    }

}
