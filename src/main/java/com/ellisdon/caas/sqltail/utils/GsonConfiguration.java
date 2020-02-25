package com.ellisdon.caas.sqltail.utils;

import com.google.gson.*;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GsonConfiguration {
    private static Gson GSON = generateStaticBuilder().create();


    private static GsonBuilder generateStaticBuilder() {
        return new GsonBuilder()
                .registerTypeAdapter(DateTime.class, (JsonSerializer<DateTime>) (json, typeOfSrc, context) -> new JsonPrimitive(ISODateTimeFormat.dateTime().print(json)))
                .registerTypeAdapter(DateTime.class, (JsonDeserializer<DateTime>) (json, typeOfT, context) -> ISODateTimeFormat.dateTime().parseDateTime(json.getAsString()))
                .enableComplexMapKeySerialization()
                .setPrettyPrinting();
    }


    @Bean
    public Gson getGsonInstance() {
        return GSON;
    }


}
