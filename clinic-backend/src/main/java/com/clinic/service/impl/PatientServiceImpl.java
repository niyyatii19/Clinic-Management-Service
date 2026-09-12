package com.clinic.service.impl;

import com.clinic.Exception.ClinicException;
import com.clinic.dto.PatientDTOs.PatientDto;
import com.clinic.dto.ResponseDto;
import com.clinic.repository.PatientRepository;
import com.clinic.utils.Enums.ResponseStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class PatientServiceImpl implements com.clinic.service.PatientService {

    private final PatientRepository patientRepository;
    private static final String PATIENT_NOT_FOUND_BY_NAME = "No patient found with name: %s";
    private static final String PATIENT_NOT_FOUND_BY_ID = "No patient found with id: %s";

    @Override
    public List<PatientDto> getAllPatients() {
        var patients  = patientRepository.findAll();
        return patients.stream()
                .map(patient -> PatientDto.builder()
                        .patientName(patient.getPatientName())
                        .contactNumber(patient.getNumber())
                        .patientDOB(patient.getPatientDateOfBirth())
                        .gender(patient.getGender())
                        .emergencyContactName(patient.getEmergencyContactName())
                        .emergencyContactNumber(patient.getEmergencyContactNumber())
                        .build())
                .toList();
    }

    @Override
    public List<PatientDto> getPatientByName(String name) {
        if(!patientRepository.existsByPatientNameIgnoreCase(name)){
            throw new ClinicException(String.format(PATIENT_NOT_FOUND_BY_NAME, name), HttpStatus.NOT_FOUND);
        }
        var patientsByName = patientRepository.findByPatientNameIgnoreCase(name);
        return patientsByName.stream()
                .map(patient -> PatientDto.builder()
                        .patientName(patient.getPatientName())
                        .patientDOB(patient.getPatientDateOfBirth())
                        .contactNumber(patient.getNumber())
                        .gender(patient.getGender())
                        .emergencyContactName(patient.getEmergencyContactName())
                        .emergencyContactNumber(patient.getEmergencyContactNumber())
                        .age(calculateAge(patient.getPatientDateOfBirth()))
                        .build())
                .toList();
    }

    @Override
    public PatientDto getPatientById(Long id) {
        var patientById = patientRepository.findById(id).orElseThrow(() -> new ClinicException(String.format(PATIENT_NOT_FOUND_BY_ID, id), HttpStatus.NOT_FOUND));
        return PatientDto.builder()
                .patientName(patientById.getPatientName())
                .patientDOB(patientById.getPatientDateOfBirth())
                .gender(patientById.getGender())
                .contactNumber(patientById.getNumber())
                .emergencyContactName(patientById.getEmergencyContactName())
                .emergencyContactNumber(patientById.getEmergencyContactNumber())
                .age(calculateAge(patientById.getPatientDateOfBirth()))
                .build();
    }

    @Override
    public ResponseDto updatePatientDetails(PatientDto patient, Long id) {
        if(!patientRepository.existsByPatientNameIgnoreCase(patient.getPatientName())){
            throw new ClinicException(String.format(PATIENT_NOT_FOUND_BY_NAME, patient.getPatientName()), HttpStatus.NOT_FOUND);
        }
        if(!patientRepository.existsById(id)){
            throw new ClinicException(String.format(PATIENT_NOT_FOUND_BY_ID, id), HttpStatus.NOT_FOUND);
        }
        var existingPatient = patientRepository.findById(id).orElseThrow(() -> new ClinicException(String.format(PATIENT_NOT_FOUND_BY_ID, id), HttpStatus.NOT_FOUND));
        try{
            Optional.ofNullable(patient.getPatientName()).ifPresent(existingPatient::setPatientName);
            Optional.ofNullable(patient.getEmergencyContactName()).ifPresent(existingPatient::setEmergencyContactName);
            Optional.ofNullable(patient.getEmergencyContactNumber()).ifPresent(existingPatient::setEmergencyContactNumber);
            Optional.ofNullable(patient.getPatientDOB()).ifPresent(existingPatient::setPatientDateOfBirth);
            Optional.ofNullable(patient.getGender()).ifPresent(existingPatient::setGender);
            Optional.ofNullable(patient.getContactNumber()).ifPresent(existingPatient::setNumber);

            patientRepository.save(existingPatient);
            return ResponseDto.builder().responseMsg("Patient Details updated successfully").status(ResponseStatus.SUCCESS).build();
        } catch (RuntimeException e){
            return ResponseDto.builder().responseMsg("Error occurred when updating patient details").status(ResponseStatus.ERROR).build();
        }
    }

    @Override
    public List<PatientDto> getPatientsForDoctors(String name) {
        if(patientRepository.existsByPatientNameIgnoreCase(name)){
            throw new ClinicException(String.format(PATIENT_NOT_FOUND_BY_NAME, name), HttpStatus.NOT_FOUND);
        }
        try{
            var patient = patientRepository.findByPatientNameIgnoreCase(name);
            return patient.stream().map(eachPatient -> PatientDto.builder()
                    .patientName(eachPatient.getPatientName())
                    .patientDOB(eachPatient.getPatientDateOfBirth())
                    .contactNumber(eachPatient.getNumber())
                    .age(calculateAge(eachPatient.getPatientDateOfBirth()))
                    .build())
                    .toList();
        } catch (ClinicException e){
            throw new ClinicException(String.format("Error fetching details of patients by name: %s", name), HttpStatus.NOT_FOUND);
        }
    }

    private Long calculateAge(LocalDate dateOfBirth){
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth cannot be null");
        }
        LocalDate today = LocalDate.now();
        if (dateOfBirth.isAfter(today)) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }
        return (long) Period.between(dateOfBirth, today).getYears();
    }
}
