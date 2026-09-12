package com.clinic.service.impl;

import com.clinic.dto.NotificationMsgDto;
import com.clinic.utils.Enums.NotificationEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${clinic.rabbitmq.exchange}")
    private String exchange;

    @Value("${clinic.rabbitmq.routing-key.appointmentBooked}")
    private String routingKeyBooking;

    @Value("${clinic.rabbitmq.routing-key.appointmentReschedule}")
    private String routingKeyReschedule;

    @Value("${clinic.rabbitmq.routing-key.appointmentCancel}")
    private String routingKeyCancel;

    @Value("${clinic.rabbitmq.routing-key.appointmentReminder}")
    private String routingKeyReminder;

    public void sendNotification(NotificationMsgDto notification) {
        String routingKey = getRoutingKeyForType(notification.getType());
        if (routingKey == null) {
            log.warn("No routing key found for notification type: {}", notification.getType());
            return;
        }
        log.info("[ Smart-Clinic ] Sending notification for {} using routing key: {}", notification.getType(), routingKey);
        rabbitTemplate.convertAndSend(exchange, routingKey, notification);
    }

    private String getRoutingKeyForType(NotificationEventType type) {
        return switch (type) {
            case APPOINTMENT_CONFIRMED -> routingKeyBooking;
            case APPOINTMENT_RESCHEDULED -> routingKeyReschedule;
            case APPOINTMENT_CANCELED -> routingKeyCancel;
            case APPOINTMENT_REMINDER -> routingKeyReminder;
            default -> null;
        };
    }
}
