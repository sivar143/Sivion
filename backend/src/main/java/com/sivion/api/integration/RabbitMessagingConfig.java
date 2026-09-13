package com.sivion.api.integration;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMessagingConfig {
    @Bean JacksonJsonMessageConverter jacksonJsonMessageConverter(){return new JacksonJsonMessageConverter();}
}
