package com.clinic.dto;

import com.clinic.utils.Enums.QueueStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class LiveQueueDto {

    private Long appointmentId;
    private LocalTime appointmentStartTime;
    private LocalTime appointmentEndTime;
    private String doctorName;
    private QueueStatus status;
}
