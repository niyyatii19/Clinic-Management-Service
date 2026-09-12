package com.clinic.dto.DoctorDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class doctorDto {

    private String doctorName;
    private String specialization;
    private Long contactNumber;
    private boolean isAvailable;
    private LocalDate docDOB;
}
