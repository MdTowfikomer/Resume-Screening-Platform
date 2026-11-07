package com.resumeai.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.time.format.DateTimeFormatter;

/**
 * Global Jackson ObjectMapper configuration.
 * Registers JavaTimeModule so Instant, LocalDateTime, ZonedDateTime
 * are serialized as ISO-8601 strings instead of throwing exceptions.
 */
@Configuration
public class JacksonConfig {

//    @Bean
//    @Primary
//    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
//        Jackson2ObjectMapperBuilder builder =
//                new Jackson2ObjectMapperBuilder()
//                        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//                        .serializers(
//                                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")))
//                        .serializationInclusion(JsonInclude.Include.NON_NULL);
//        return new MappingJackson2HttpMessageConverter(builder.build());
//    }

    @Primary
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}