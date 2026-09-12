package com.clinic.dto.AppointmentDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlotDto {

    private LocalTime slotStartTime;
    private LocalTime slotEndTime;
    private boolean isAvailable;
}
