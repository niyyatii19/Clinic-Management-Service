package com.NotificationService.Service;

import com.NotificationService.Dto.NotificationDto;
import com.NotificationService.utils.Enums.NotificationEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender javaMailSender;

    public void sendMail(NotificationDto dto) {
        String subject = getSubject(dto.getType());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(dto.getEmail());
        message.setSubject(subject);
        message.setText(dto.getMessage());
        message.setSentDate(Date.from(Instant.now()));

        javaMailSender.send(message);
        log.info("Email sent to {} with subject '{}'", dto.getEmail(), subject);
    }

    private String getSubject(NotificationEventType type) {
        return switch (type) {
            case APPOINTMENT_CONFIRMED -> "Your Appointment is Confirmed";
            case APPOINTMENT_CANCELED -> "Your Appointment has been Canceled";
            case APPOINTMENT_RESCHEDULED -> "Your Appointment has been Rescheduled";
            case APPOINTMENT_REMINDER -> "Appointment Reminder";
        };
    }
}

