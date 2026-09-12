package com.NotificationService.Dto;

import com.NotificationService.utils.Enums.NotificationEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class NotificationDto {

    private String email;
    private Long contactNumber;
    private String subject;
    private String message;
    private NotificationEventType type;
}
