package com.geek.sc.rabbitmq;

import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.rabbit.RabbitExtendedBindingProperties;
import org.springframework.cloud.stream.binder.rabbit.RabbitMessageChannelBinder;
import org.springframework.cloud.stream.binder.rabbit.config.RabbitMessageChannelBinderConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.integration.codec.Codec;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Import({RabbitMessageChannelBinderConfiguration.class})
@EnableScheduling
public class RabbitMQBinderConfig {

    @Autowired
    private Codec codec;

    @Autowired
    private ConnectionFactory rabbitConnectionFactory;

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private RabbitExtendedBindingProperties rabbitExtendedBindingProperties;

    @Autowired
    @Qualifier("gZipPostProcessor")
    MessagePostProcessor gZipPostProcessor;

    @Autowired
    @Qualifier("deCompressingPostProcessor")
    MessagePostProcessor deCompressingPostProcessor;

    @Bean
    @ConfigurationProperties(prefix = "spring.cloud.stream")
    public RabbitMQProperties rabbitMQProperties() {
        return new RabbitMQProperties();
    }

    @Bean
    @Primary
    RabbitMessageChannelBinder rabbitMessageChannelBinder() {
        RabbitMQBinder binder = new RabbitMQBinder(rabbitConnectionFactory, rabbitProperties);
        binder.setCodec(codec);
        binder.setCompressingPostProcessor(gZipPostProcessor);
        binder.setDecompressingPostProcessor(deCompressingPostProcessor);
        binder.setExtendedBindingProperties(rabbitExtendedBindingProperties);
        return binder;
    }
}