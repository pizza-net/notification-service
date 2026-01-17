package com.pizzanet.notificationservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String ORDER_STATUS_QUEUE = "order.status.queue";
    
    // Exchange name
    public static final String ORDER_EXCHANGE = "order.exchange";
    
    // Routing key
    public static final String ORDER_STATUS_ROUTING_KEY = "order.status.changed";

    /**
     * Definicja kolejki RabbitMQ dla statusów zamówień
     */
    @Bean
    public Queue orderStatusQueue() {
        return new Queue(ORDER_STATUS_QUEUE, true); // durable = true
    }

    /**
     * Definicja exchange typu "topic" dla zamówień
     */
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    /**
     * Binding - łączenie kolejki z exchange przez routing key
     */
    @Bean
    public Binding orderStatusBinding(Queue orderStatusQueue, TopicExchange orderExchange) {
        return BindingBuilder
                .bind(orderStatusQueue)
                .to(orderExchange)
                .with(ORDER_STATUS_ROUTING_KEY);
    }

    /**
     * Message converter - serializacja/deserializacja JSON z obsługą LocalDateTime
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * RabbitTemplate z JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
