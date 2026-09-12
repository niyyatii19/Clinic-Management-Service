package com.clinic.service.impl;

import com.clinic.Exception.ClinicException;
import com.clinic.Exception.ClinicResponseException;
import com.clinic.dto.DoctorDTOs.DoctorShiftTimeDto;
import com.clinic.dto.DoctorDTOs.doctorDto;
import com.clinic.dto.ResponseDto;
import com.clinic.models.Doctor;
import com.clinic.models.DoctorScheduleEntity;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.DoctorScheduleRepository;
import com.clinic.repository.PatientRepository;
import com.clinic.utils.Enums.ResponseStatus;
import com.clinic.utils.Enums.ScheduleRecordType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DoctorServiceImplTest {

    @InjectMocks
    private DoctorServiceImpl doctorService;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorScheduleRepository doctorScheduleRepository;

    private static final LocalDate CURRENT_DATE = LocalDate.of(2025, 8, 3);
    private static final LocalTime CURRENT_TIME = LocalTime.of(10, 0);

    @BeforeEach
    void setUp() {MockitoAnnotations.openMocks(this);}

    @Test
    void updateDoctorDetails_successfulUpdate_returnsSuccessResponse() {
        // Given
        Long docId = 1L;
        Doctor existingDoctor = new Doctor();
        existingDoctor.setId(docId);

        when(doctorRepository.findById(docId)).thenReturn(Optional.of(existingDoctor));

        doctorDto dto = doctorDto.builder()
                .doctorName("Dr. Smith")
                .specialization("Cardiology")
                .contactNumber(1234567890L)
                .docDOB(LocalDate.of(1980, 5, 10))
                .isAvailable(true)
                .build();

        // When
        ResponseDto response = doctorService.updateDoctorDetails(dto, docId);

        // Then
        assertEquals(ResponseStatus.SUCCESS, response.getStatus());
        assertTrue(response.getResponseMsg().contains("Doctor details updated successfully"));

        verify(doctorRepository, times(1)).save(existingDoctor);
    }

    @Test
    void updateDoctorDetails_doctorNotFound_throwsClinicException() {
        // Given
        Long docId = 100L;
        when(doctorRepository.findById(docId)).thenReturn(Optional.empty());

        doctorDto dto = doctorDto.builder()
                .doctorName("Test Doc")
                .build();

        // When
        ClinicException exception = assertThrows(ClinicException.class,
                () -> doctorService.updateDoctorDetails(dto, docId));

        // Then
        assertEquals("Doctor not found for id: 100", exception.getMessage());
        assertEquals(404, HttpStatus.NOT_FOUND.value());

        verify(doctorRepository, never()).save(any());
    }

    @Test
    void updateDoctorDetails_partialFieldsProvided_onlyThoseAreUpdated() {
        // Given
        Long docId = 5L;
        Doctor doctor = new Doctor();
        doctor.setId(docId);
        doctor.setDoctorName("Old Name");
        doctor.setSpecializations("Old Spec");

        when(doctorRepository.findById(docId)).thenReturn(Optional.of(doctor));

        doctorDto dto = doctorDto.builder()
                .doctorName("New Name")
                .build(); // Only name is updated

        // When
        doctorService.updateDoctorDetails(dto, docId);

        // Then
        assertEquals("New Name", doctor.getDoctorName());
        assertEquals("Old Spec", doctor.getSpecializations()); // Unchanged
        verify(doctorRepository).save(doctor);
    }

    @Test
    void updateDoctorDetails_handlesExceptionAndReturnsErrorResponse() {
        // Given
        Long docId = 100L;
        Doctor existingDoctor = new Doctor();
        existingDoctor.setDoctorName("Dr. Old");

        when(doctorRepository.findById(docId)).thenReturn(Optional.of(existingDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenThrow(new RuntimeException("DB error"));

        doctorDto doctorUpdate = doctorDto.builder()
                .doctorName("Dr. Updated")
                .specialization("ENT")
                .build();

        // When
        var response = doctorService.updateDoctorDetails(doctorUpdate, docId);

        // Then
        assertEquals("Error occurred while updating doctor details: DB error", response.getResponseMsg());
        assertEquals(ResponseStatus.ERROR, response.getStatus());

        verify(doctorRepository).findById(docId);
        verify(doctorRepository).save(any(Doctor.class));
    }


    @Test
    void getDoctorsByName_returnsMatchingDoctors() {
        // Given
        String name = "Dr. Strange";
        Doctor doctor = new Doctor();
        doctor.setDoctorName(name);
        doctor.setSpecializations("Neurology");

        DoctorScheduleEntity schedule = new DoctorScheduleEntity();
        schedule.setShiftDate(LocalDate.now());
        schedule.setIsAvailable(true);

        doctor.setSchedules(List.of(schedule));

        when(doctorRepository.findByDoctorNameIgnoreCase(name)).thenReturn(List.of(doctor));

        // When
        var result = doctorService.getDoctorsByName(name);

        // Then
        assertEquals(1, result.size());
        assertEquals(name, result.getFirst().getDoctorName());
        assertEquals("Neurology", result.getFirst().getSpecialization());
        assertTrue(result.getFirst().isAvailable());

        verify(doctorRepository, times(1)).findByDoctorNameIgnoreCase(name);
    }

    @Test
    void getDoctorsBySpecialization_returnsMatchingDoctors() {
        // Given
        String specialization = "Dermatology";
        Doctor doctor = new Doctor();
        doctor.setDoctorName("Dr. Skinner");
        doctor.setSpecializations(specialization);

        DoctorScheduleEntity schedule = new DoctorScheduleEntity();
        schedule.setShiftDate(LocalDate.now());
        schedule.setIsAvailable(false);

        doctor.setSchedules(List.of(schedule));

        when(doctorRepository.findBySpecializationsIgnoreCase(specialization)).thenReturn(List.of(doctor));

        // When
        var result = doctorService.getDoctorsBySpecialization(specialization);

        // Then
        assertEquals(1, result.size());
        assertEquals("Dr. Skinner", result.getFirst().getDoctorName());
        assertEquals("Dermatology", result.getFirst().getSpecialization());
        assertFalse(result.getFirst().isAvailable());

        verify(doctorRepository, times(1)).findBySpecializationsIgnoreCase(specialization);
    }

    @Test
    void getDoctorsByName_returnsEmptyList_whenNoMatch() {
        when(doctorRepository.findByDoctorNameIgnoreCase("Unknown")).thenReturn(List.of());
        var result = doctorService.getDoctorsByName("Unknown");
        assertTrue(result.isEmpty());
    }

    @Test
    void getDoctorsBySpecialization_returnsEmptyList_whenNoMatch() {
        when(doctorRepository.findBySpecializationsIgnoreCase("UnknownSpec")).thenReturn(List.of());
        var result = doctorService.getDoctorsBySpecialization("UnknownSpec");
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllDoctors_returnsAllDoctors() {
        // Given
        Doctor doctor1 = new Doctor();
        doctor1.setDoctorName("Dr. A");
        doctor1.setSpecializations("Cardiology");

        DoctorScheduleEntity schedule1 = new DoctorScheduleEntity();
        schedule1.setShiftDate(LocalDate.now());
        schedule1.setIsAvailable(true);
        doctor1.setSchedules(List.of(schedule1));

        Doctor doctor2 = new Doctor();
        doctor2.setDoctorName("Dr. B");
        doctor2.setSpecializations("Pediatrics");

        DoctorScheduleEntity schedule2 = new DoctorScheduleEntity();
        schedule2.setShiftDate(LocalDate.now());
        schedule2.setIsAvailable(false);
        doctor2.setSchedules(List.of(schedule2));

        when(doctorRepository.findAllDoctorsWithAvailabilityForToday()).thenReturn(List.of(doctor1, doctor2));

        // When
        var result = doctorService.getAllDoctors();

        // Then
        assertEquals(2, result.size());

        assertEquals("Dr. A", result.get(0).getDoctorName());
        assertEquals("Cardiology", result.get(0).getSpecialization());
        assertTrue(result.get(0).isAvailable());

        assertEquals("Dr. B", result.get(1).getDoctorName());
        assertEquals("Pediatrics", result.get(1).getSpecialization());
        assertFalse(result.get(1).isAvailable());

        verify(doctorRepository, times(1)).findAllDoctorsWithAvailabilityForToday();
    }

    @Nested
    class RecordDoctorShiftTimingsTests {

        private Doctor doctor;
        private DoctorShiftTimeDto dto;

        @BeforeEach
        void init() {
            doctor = new Doctor();
            doctor.setId(1L);
            doctor.setDoctorName("Dr. Smith");
            doctor.setSchedules(new ArrayList<>());

            dto = DoctorShiftTimeDto.builder()
                    .recordType(ScheduleRecordType.NEW_ENTRY)
                    .shiftStartTime(LocalTime.of(9, 0))
                    .shiftEndTime(LocalTime.of(17, 0))
                    .shiftDate(LocalDate.now())
                    .isAvailable(true)
                    .build();
        }

        @Test
        void testNewEntry_Success() {
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(doctorScheduleRepository.save(any())).thenReturn(new DoctorScheduleEntity());

            var response = doctorService.recordDoctorShiftTimings(dto, 1L);

            assertEquals(ResponseStatus.SUCCESS, response.getStatus());
            assertEquals("Shift details updated successfully", response.getResponseMsg());
            verify(doctorScheduleRepository, times(1)).save(any());
        }

        @Test
        void testNewEntry_AlreadyExists() {
            DoctorScheduleEntity existing = new DoctorScheduleEntity();
            existing.setShiftDate(dto.getShiftDate());
            doctor.getSchedules().add(existing);

            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

            var response = doctorService.recordDoctorShiftTimings(dto, 1L);

            assertEquals(ResponseStatus.ERROR, response.getStatus());
            assertTrue(response.getResponseMsg().contains("Shift already exists"));
            verify(doctorScheduleRepository, never()).save(any());
        }

        @Test
        void testUpdate_Success() {
            dto.setRecordType(ScheduleRecordType.UPDATE);

            DoctorScheduleEntity existing = new DoctorScheduleEntity();
            existing.setShiftDate(dto.getShiftDate());
            doctor.getSchedules().add(existing);

            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(doctorScheduleRepository.save(any())).thenReturn(existing);

            var response = doctorService.recordDoctorShiftTimings(dto, 1L);

            assertEquals(ResponseStatus.SUCCESS, response.getStatus());
            verify(doctorScheduleRepository, times(1)).save(existing);
        }

        @Test
        void testUpdate_NotFound() {
            dto.setRecordType(ScheduleRecordType.UPDATE);

            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

            var response = doctorService.recordDoctorShiftTimings(dto, 1L);

            assertEquals(ResponseStatus.ERROR, response.getStatus());
            assertTrue(response.getResponseMsg().contains("No existing shift"));
            verify(doctorScheduleRepository, never()).save(any());
        }

        @Test
        void testDoctorNotFound() {
            when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

            var ex = assertThrows(ClinicException.class, () -> doctorService.recordDoctorShiftTimings(dto, 1L));

            assertEquals("Doctor not found with id 1", ex.getMessage());
        }

        @Test
        void testExceptionCatchBlock() {
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(doctorScheduleRepository.save(any())).thenThrow(new RuntimeException("Simulated DB failure"));

            var ex = assertThrows(ClinicResponseException.class, () -> doctorService.recordDoctorShiftTimings(dto, 1L));

            assertTrue(ex.getMessage().contains("Error updating or recording the doctor shift details"));
        }
    }

    @Nested
    class UpdateDoctorAvailabilityTests {

        @Test
        void shouldUpdateDoctorAvailabilityWhenShiftEnded() {
            // Arrange
            Doctor mockDoctor = new Doctor();
            mockDoctor.setId(1L);
            mockDoctor.setDoctorName("Sara");

            DoctorScheduleEntity schedule = new DoctorScheduleEntity();
            schedule.setShiftEndTime(CURRENT_TIME.minusMinutes(30));
            schedule.setShiftDate(CURRENT_DATE);
            schedule.setIsAvailable(true);
            schedule.setDoctor(mockDoctor);

            List<DoctorScheduleEntity> schedules = List.of(schedule);

            when(doctorScheduleRepository.findByShiftDateAndIsAvailableTrueAndShiftEndTimeBefore(any(), any()))
                    .thenReturn(schedules);

            // Act
            doctorService.updateDoctorAvailability();

            // Assert
            assertFalse(schedule.getIsAvailable());
            verify(doctorScheduleRepository).saveAll(schedules);
        }

        @Test
        void shouldNotUpdateWhenNoDoctorsFound() {
            // Arrange
            when(doctorScheduleRepository.findByShiftDateAndIsAvailableTrueAndShiftEndTimeBefore(eq(CURRENT_DATE), any()))
                    .thenReturn(Collections.emptyList());

            // Act
            doctorService.updateDoctorAvailability();

            // Assert
            verify(doctorScheduleRepository, never()).saveAll(any());
        }

        @Test
        void shouldThrowClinicResponseExceptionWhenRepositoryFails() {
            // Arrange
            when(doctorScheduleRepository.findByShiftDateAndIsAvailableTrueAndShiftEndTimeBefore(any(), any()))
                    .thenThrow(new RuntimeException("DB error"));

            // Act & Assert
            ClinicResponseException thrown = assertThrows(ClinicResponseException.class,
                    () -> doctorService.updateDoctorAvailability());

            assertEquals("DB error", thrown.getMessage());
        }
    }
}
