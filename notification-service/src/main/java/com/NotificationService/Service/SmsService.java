package com.NotificationService.Service;

import com.NotificationService.Dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

        public void sendSms(NotificationDto dto) {
            // Integration with SMS provider goes here
            log.info("SMS sent to {}: {}", dto.getContactNumber(), dto.getMessage());
        }
}
