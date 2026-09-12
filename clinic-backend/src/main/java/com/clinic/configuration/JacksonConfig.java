package com.clinic.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@Configuration
public class JacksonConfig {

        private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
        @Bean
        @Primary
        public ObjectMapper objectMapper() {
            ObjectMapper objectMapper = new ObjectMapper();

            JavaTimeModule javaTimeModule = new JavaTimeModule();
            DateTimeFormatter flexibleTimeFormatter = new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendValue(ChronoField.HOUR_OF_DAY)
                    .appendLiteral(':')
                    .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                    .toFormatter();

            javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(TIME_FORMATTER));
            javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(flexibleTimeFormatter));

            objectMapper.registerModule(javaTimeModule);
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return objectMapper;
        }
}
