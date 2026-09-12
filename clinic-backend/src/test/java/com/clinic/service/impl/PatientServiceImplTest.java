package com.clinic.service.impl;

import com.clinic.Exception.ClinicException;
import com.clinic.dto.PatientDTOs.PatientDto;
import com.clinic.dto.ResponseDto;
import com.clinic.models.Patient;
import com.clinic.repository.PatientRepository;
import com.clinic.utils.Enums.ResponseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import java.lang.reflect.Method;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PatientServiceImplTest {

    private PatientRepository patientRepository;
    private PatientServiceImpl patientService;

    @BeforeEach
    void setup() {
        patientRepository = mock(PatientRepository.class);
        patientService = new PatientServiceImpl(patientRepository);
    }

    @Test
    void getAllPatients_returnsAllPatientDtos() {
        Patient patient = new Patient();
        patient.setPatientName("John");
        patient.setNumber(12345L);
        patient.setPatientDateOfBirth(LocalDate.of(2000, 1, 1));
        patient.setGender("Male");
        patient.setEmergencyContactName("Jane");
        patient.setEmergencyContactNumber(54321L);

        when(patientRepository.findAll()).thenReturn(List.of(patient));

        List<PatientDto> result = patientService.getAllPatients();

        assertEquals(1, result.size());
        assertEquals("John", result.getFirst().getPatientName());
    }

    @Test
    void getPatientByName_throwsExceptionWhenExists() {
        when(patientRepository.existsByPatientNameIgnoreCase("John")).thenReturn(false);

        ClinicException ex = assertThrows(ClinicException.class, () ->
                patientService.getPatientByName("John"));
        assertTrue(ex.getMessage().contains("No patient found with name: John"));
    }

    @Test
    void getPatientByName_returnsListOfPatients() {
        // Fix 1: exists check should return true (patient exists)
        when(patientRepository.existsByPatientNameIgnoreCase("John")).thenReturn(true);

        // Fix 2: set all necessary fields to avoid NPE and exceptions
        Patient patient = new Patient();
        patient.setPatientName("John");
        patient.setPatientDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setNumber(1234567890L);
        patient.setGender("Male");
        patient.setEmergencyContactName("Jane");
        patient.setEmergencyContactNumber(9876543210L);

        when(patientRepository.findByPatientNameIgnoreCase("John")).thenReturn(List.of(patient));

        List<PatientDto> result = patientService.getPatientByName("John");

        assertEquals(1, result.size());
        assertEquals("John", result.getFirst().getPatientName());
        assertEquals(1990, result.getFirst().getPatientDOB().getYear()); // optional
    }


    @Test
    void getPatientById_throwsExceptionWhenNotExists() {
        when(patientRepository.existsById(1L)).thenReturn(false);

        ClinicException ex = assertThrows(ClinicException.class, () ->
                patientService.getPatientById(1L));
        assertTrue(ex.getMessage().contains("No patient found with id: 1"));
    }

    @Test
    void getPatientById_returnsPatient() {
        Patient patient = new Patient();
        patient.setPatientName("John");
        patient.setPatientDateOfBirth(LocalDate.of(1985, 7, 26));
        when(patientRepository.existsById(1L)).thenReturn(true);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        PatientDto result = patientService.getPatientById(1L);
        assertTrue(Objects.nonNull(result));
        assertEquals("John", result.getPatientName());
    }

    @Test
    void updatePatientDetails_throwsIfNameExists() {
        PatientDto dto = PatientDto.builder().patientName("John").build();
        when(patientRepository.existsByPatientNameIgnoreCase("John")).thenReturn(true);

        assertThrows(ClinicException.class, () ->
                patientService.updatePatientDetails(dto, 1L));
    }

    @Test
    void updatePatientDetails_throwsIfIdNotExists() {
        PatientDto dto = PatientDto.builder().patientName("John").build();
        when(patientRepository.existsByPatientNameIgnoreCase("John")).thenReturn(false);
        when(patientRepository.existsById(1L)).thenReturn(false);

        assertThrows(ClinicException.class, () ->
                patientService.updatePatientDetails(dto, 1L));
    }

    @Test
    void updatePatientDetails_successfullyUpdates() {
        PatientDto dto = PatientDto.builder().patientName("New Name").build();
        Patient patient = new Patient();
        patient.setPatientName("Old Name");

        when(patientRepository.existsByPatientNameIgnoreCase("New Name")).thenReturn(true);
        when(patientRepository.existsById(1L)).thenReturn(true);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        ResponseDto response = patientService.updatePatientDetails(dto, 1L);

        assertEquals(ResponseStatus.SUCCESS, response.getStatus());
        assertEquals("Patient Details updated successfully", response.getResponseMsg());
        verify(patientRepository).save(patient);
    }

    @Test
    void updatePatientDetails_catchesExceptionAndReturnsErrorResponse() {
        PatientDto dto = PatientDto.builder().patientName("New Name").build();
        Patient patient = new Patient();

        // Fix: mock patient name existence as true
        when(patientRepository.existsByPatientNameIgnoreCase("New Name")).thenReturn(true);
        when(patientRepository.existsById(1L)).thenReturn(true);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        // Simulate DB error on save
        doThrow(new RuntimeException("DB error")).when(patientRepository).save(any(Patient.class));

        ResponseDto response = patientService.updatePatientDetails(dto, 1L);

        assertEquals(ResponseStatus.ERROR, response.getStatus());
        assertEquals("Error occurred when updating patient details", response.getResponseMsg());
    }

    @Test
    void getPatientsForDoctors_throwsIfPatientExists() {
        when(patientRepository.existsByPatientNameIgnoreCase("John")).thenReturn(true);
        assertThrows(ClinicException.class, () ->
                patientService.getPatientsForDoctors("John"));
    }

    @Test
    void getPatientsForDoctors_returnsAgeCalculated() {
        Patient patient = new Patient();
        patient.setPatientName("John");
        patient.setNumber(123L);
        patient.setPatientDateOfBirth(LocalDate.of(2000, 1, 1));

        when(patientRepository.existsByPatientNameIgnoreCase("John")).thenReturn(false);
        when(patientRepository.findByPatientNameIgnoreCase("John")).thenReturn(List.of(patient));

        List<PatientDto> result = patientService.getPatientsForDoctors("John");

        assertEquals(1, result.size());
        assertEquals("John", result.getFirst().getPatientName());
        assertNotNull(result.getFirst().getAge());
    }

    @Test
    void calculateAge_throwsIfDateIsNullOrFuture() throws Exception {
        Method method = patientService.getClass().getDeclaredMethod("calculateAge", LocalDate.class);
        method.setAccessible(true);

        Executable nullInvocation = () -> {
            try {
                method.invoke(patientService, (Object) null);
            } catch (Exception e) {
                throw e.getCause();
            }
        };
        assertThrows(IllegalArgumentException.class, nullInvocation);

        Executable futureInvocation = () -> {
            try {
                method.invoke(patientService, LocalDate.now().plusDays(1));
            } catch (Exception e) {
                throw e.getCause();
            }
        };
        assertThrows(IllegalArgumentException.class, futureInvocation);
    }

}


