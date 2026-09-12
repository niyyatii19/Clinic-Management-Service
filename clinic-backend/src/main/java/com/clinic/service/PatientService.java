package com.clinic.service;

import com.clinic.dto.PatientDTOs.PatientDto;
import com.clinic.dto.ResponseDto;

import java.util.List;

public interface PatientService {

    List<PatientDto> getAllPatients();
    List<PatientDto> getPatientByName(String name);
    PatientDto getPatientById(Long id);
    ResponseDto updatePatientDetails(PatientDto patient, Long id);
    List<PatientDto> getPatientsForDoctors(String name);
}
