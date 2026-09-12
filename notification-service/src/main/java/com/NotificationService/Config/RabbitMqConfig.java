package com.NotificationService.Config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${clinic.rabbitmq.queue.appointmentBooked}")
    private String bookingQueue;
    @Value("${clinic.rabbitmq.queue.appointmentReschedule}")
    private String rescheduleQueue;
    @Value("${clinic.rabbitmq.queue.appointmentCancel}")
    private String cancelQueue;
    @Value("${clinic.rabbitmq.queue.appointmentReminder}")
    private String reminderQueue;

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
