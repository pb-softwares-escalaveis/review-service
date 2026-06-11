package com.service.review.config;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.JsonMessageConverter;

@Configuration
public class KafkaConfig {

    @Bean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
    }

    @Bean
    public JsonMessageConverter jsonMessageConverter(JsonMapper jsonMapper) {
        return new JsonMessageConverter(jsonMapper);
    }

}