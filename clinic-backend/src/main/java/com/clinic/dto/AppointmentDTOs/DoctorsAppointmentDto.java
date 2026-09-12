package com.clinic.dto.AppointmentDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorsAppointmentDto {

    private String patientName;
    private LocalDate dob;
    private String reason;
    private LocalTime time;
    private Long age;
}
