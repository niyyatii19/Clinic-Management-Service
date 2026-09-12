package com.clinic.dto;

import com.clinic.utils.Enums.NotificationEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMsgDto {

    private String email;
    private Long contactNumber;
    private String subject;
    private String message;
    private NotificationEventType type;
}
