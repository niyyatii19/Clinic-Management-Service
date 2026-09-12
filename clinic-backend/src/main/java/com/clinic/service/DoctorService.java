package com.clinic.service;

import com.clinic.dto.DoctorDTOs.DoctorShiftTimeDto;
import com.clinic.dto.ResponseDto;
import com.clinic.dto.DoctorDTOs.doctorDto;

import java.util.List;

public interface DoctorService {

    List<doctorDto> getAllDoctors();
    List<doctorDto> getDoctorsByName(String docName);
    List<doctorDto> getDoctorsBySpecialization(String docSpecialization);
    ResponseDto updateDoctorDetails(doctorDto doctor, Long id);
    ResponseDto recordDoctorShiftTimings(DoctorShiftTimeDto doctorShiftTimeDto, Long docId);
    void updateDoctorAvailability();
}
