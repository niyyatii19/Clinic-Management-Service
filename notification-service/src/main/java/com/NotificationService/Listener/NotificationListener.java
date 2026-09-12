package com.NotificationService.Listener;

import com.NotificationService.Dto.NotificationDto;
import com.NotificationService.Service.MailService;
import com.NotificationService.Service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final MailService mailService;
    private final SmsService smsService;

    @RabbitListener(queues = "${clinic.rabbitmq.queue.appointmentBooked}")
    public void handleAppointmentBooked(NotificationDto notification) {
        notification.setSubject("Your Appointment is Confirmed");
        processNotification(notification);
    }

    @RabbitListener(queues = "${clinic.rabbitmq.queue.appointmentReschedule}")
    public void handleAppointmentRescheduled(NotificationDto notification) {
        notification.setSubject("Your Appointment has been Rescheduled");
        processNotification(notification);
    }

    @RabbitListener(queues = "${clinic.rabbitmq.queue.appointmentCancel}")
    public void handleAppointmentCanceled(NotificationDto notification) {
        notification.setSubject("Your Appointment has been Canceled");
        processNotification(notification);
    }

    @RabbitListener(queues = "${clinic.rabbitmq.queue.appointmentReminder}")
    public void handleAppointmentReminder(NotificationDto notification) {
        notification.setSubject("Appointment Reminder");
        processNotification(notification);
    }

    private void processNotification(NotificationDto notification) {
        log.info("Received notification request: {}", notification);
        try {
            if (notification.getEmail() != null && !notification.getEmail().isBlank()) {
                mailService.sendMail(notification);
                log.info("Email sent to {}", notification.getEmail());
            }
            if (notification.getContactNumber() != null) {
                smsService.sendSms(notification);
                log.info("SMS sent to {}", notification.getContactNumber());
            }
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage(), e);
        }
    }
}
