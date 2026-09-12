package com.clinic.dto.PatientDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientDto {

    private String patientName;
    private LocalDate patientDOB;
    private Long contactNumber;
    private String emergencyContactName;
    private String gender;
    private Long emergencyContactNumber;
    private Long age;
}
