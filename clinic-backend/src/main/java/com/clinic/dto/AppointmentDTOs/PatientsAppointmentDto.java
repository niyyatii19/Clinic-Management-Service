package com.clinic.dto.AppointmentDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientsAppointmentDto {
    private Long appointmentId;
    private LocalDateTime newTime;
}
