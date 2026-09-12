package com.clinic.configuration.RabbitMq;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${clinic.rabbitmq.exchange}")
    private String exchange;
    @Value("${clinic.rabbitmq.queue.appointmentBooked}")
    private String bookingQueue;
    @Value("${clinic.rabbitmq.queue.appointmentReschedule}")
    private String rescheduleQueue;
    @Value("${clinic.rabbitmq.queue.appointmentCancel}")
    private String cancelQueue;
    @Value("${clinic.rabbitmq.queue.appointmentReminder}")
    private String reminderQueue;
    @Value("${clinic.rabbitmq.routing-key.appointmentBooked}")
    private String routingKeyBooking;
    @Value("${clinic.rabbitmq.routing-key.appointmentReschedule}")
    private String routingKeyReschedule;
    @Value("${clinic.rabbitmq.routing-key.appointmentCancel}")
    private String routingKeyCancel;
    @Value("${clinic.rabbitmq.routing-key.appointmentReminder}")
    private String routingKeyReminder;

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    public Queue bookingQueue() {
        return new Queue(bookingQueue);
    }

    @Bean
    public Queue rescheduleQueue() {
        return new Queue(rescheduleQueue);
    }

    @Bean
    public Queue cancelQueue() {
        return new Queue(cancelQueue);
    }

    @Bean
    public Queue reminderQueue() {
        return new Queue(reminderQueue);
    }

    @Bean
    public Binding bookingBind(Queue bookingQueue, DirectExchange exchange) {
        return BindingBuilder.bind(bookingQueue).to(exchange).with(routingKeyBooking);
    }

    @Bean
    public Binding rescheduleBind(Queue rescheduleQueue, DirectExchange exchange) {
        return BindingBuilder.bind(rescheduleQueue).to(exchange).with(routingKeyReschedule);
    }

    @Bean
    public Binding cancelBind(Queue cancelQueue, DirectExchange exchange) {
        return BindingBuilder.bind(cancelQueue).to(exchange).with(routingKeyCancel);
    }

    @Bean
    public Binding reminderBind(Queue reminderQueue, DirectExchange exchange) {
        return BindingBuilder.bind(reminderQueue).to(exchange).with(routingKeyReminder);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
