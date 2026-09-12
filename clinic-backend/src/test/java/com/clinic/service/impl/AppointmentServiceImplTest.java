package com.clinic.service.impl;

import com.clinic.Exception.ClinicException;
import com.clinic.Exception.ClinicResponseException;
import com.clinic.dto.AppointmentDTOs.AppointmentRequestDto;
import com.clinic.dto.AppointmentDTOs.RescheduleRequestDto;
import com.clinic.models.*;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.DoctorScheduleRepository;
import com.clinic.repository.PatientRepository;
import com.clinic.utils.Enums.AppointmentStatus;
import com.clinic.utils.Enums.ResponseStatus;
import com.clinic.utils.UtilClasses.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

        @Mock
        AppointmentRepository appointmentRepository;
        @Mock
        DoctorRepository doctorRepository;
        @Mock
        PatientRepository patientRepository;
        @Mock
        DoctorScheduleRepository doctorScheduleRepository;
        @Mock
        CustomUserDetails userUtils;
        @Mock
        NotificationService notificationService;

        @InjectMocks
        AppointmentServiceImpl appointmentService;

        private Doctor doctor;
        private Patient patient;

        @BeforeEach
        void init() {
            doctor = Doctor.builder()
                    .id(1L)
                    .doctorName("Smith")
                    .docDateOfBirth(LocalDate.of(1990, 8, 16))
                    .schedules(List.of(DoctorScheduleEntity.builder().isAvailable(true).build()))
                    .build();

            patient = Patient.builder()
                    .id(2L)
                    .patientName("John Doe")
                    .number(1234567890L)
                    .patientDateOfBirth(LocalDate.of(1990, 8, 16))
                    .user(User.builder().email("patient@example.com").build())
                    .build();
        }

        @Nested
        class BookAppointmentTests {

            @Test
            void shouldThrowIfDateBeforeToday() {
                AppointmentRequestDto dto = getValidRequestDto();
                dto.setAppointmentDate(LocalDate.now().minusDays(1));

                var ex = assertThrows(ClinicException.class, () -> appointmentService.bookAppointment(dto));
                assertTrue(ex.getMessage().contains("The booking date needs to be today"));
            }

            @Test
            void shouldThrowIfDateAfterTwoDays() {
                AppointmentRequestDto dto = getValidRequestDto();
                dto.setAppointmentDate(LocalDate.now().plusDays(3));

                var ex = assertThrows(ClinicException.class, () -> appointmentService.bookAppointment(dto));
                assertTrue(ex.getMessage().contains("The booking date needs to be today"));
            }

            @Test
            void shouldReturnErrorIfDoctorNotAvailable() {
                AppointmentRequestDto dto = getValidRequestDto();
                doctor.setSchedules(List.of(DoctorScheduleEntity.builder().isAvailable(false).build()));

                when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
                when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

                var res = appointmentService.bookAppointment(dto);
                assertEquals(ResponseStatus.ERROR, res.getStatus());
                assertTrue(res.getResponseMsg().contains("Doctor is not available"));
            }

            @Test
            void shouldThrowIfOutsideWorkingHours() {
                AppointmentRequestDto dto = getValidRequestDto();
                dto.setAppointmentStartTime(LocalTime.of(8, 0));

                when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
                when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

                var ex = assertThrows(ClinicException.class, () -> appointmentService.bookAppointment(dto));
                assertTrue(ex.getMessage().contains("outside working hours"));
            }

            @Test
            void shouldThrowIfDurationTooLong() {
                AppointmentRequestDto dto = getValidRequestDto();
                dto.setAppointmentEndTime(dto.getAppointmentStartTime().plusMinutes(31));

                when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
                when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

                var ex = assertThrows(ClinicException.class, () -> appointmentService.bookAppointment(dto));
                assertTrue(ex.getMessage().contains("Slot duration exceeds"));
            }

            @Test
            void shouldReturnErrorIfSlotAlreadyBooked() {
                AppointmentRequestDto dto = getValidRequestDto();

                when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
                when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
                when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndTimeOverlap(
                        anyLong(), any(), any(), any())).thenReturn(true);

                var res = appointmentService.bookAppointment(dto);
                assertEquals(ResponseStatus.ERROR, res.getStatus());
                assertTrue(res.getResponseMsg().contains("already booked"));
            }

            @Test
            void shouldBookSuccessfully() {
                AppointmentRequestDto dto = getValidRequestDto();

                when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
                when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
                when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndTimeOverlap(anyLong(), any(), any(), any())).thenReturn(false);

                var res = appointmentService.bookAppointment(dto);
                assertEquals(ResponseStatus.SUCCESS, res.getStatus());
                verify(notificationService).sendNotification(any());
            }

            private AppointmentRequestDto getValidRequestDto() {
                return AppointmentRequestDto.builder()
                        .patientId(2L)
                        .doctorId(1L)
                        .appointmentDate(LocalDate.now())
                        .appointmentStartTime(LocalTime.of(10, 0))
                        .appointmentEndTime(LocalTime.of(10, 30))
                        .reasonForVisit("Checkup")
                        .build();
            }
        }

        @Nested
        class GetAvailableSlotsForDoctorTests {

            @Test
            void shouldThrowIfDateInvalid() {
                LocalDate past = LocalDate.now().minusDays(1);
                assertThrows(ClinicException.class, () -> appointmentService.getAvailableSlotsForDoctor(1L, past));
            }

            @Test
            void shouldThrowIfDoctorNotAvailable() {
                var schedule = DoctorScheduleEntity.builder()
                        .shiftStartTime(LocalTime.of(9, 0))
                        .shiftEndTime(LocalTime.of(10, 0))
                        .isAvailable(false)
                        .build();

                when(doctorScheduleRepository.findByDocIdAndShiftDate(any(), any()))
                        .thenReturn(Optional.of(schedule));

                LocalDate appointmentDate = LocalDate.now();

                assertThrows(ClinicResponseException.class,
                        () -> appointmentService.getAvailableSlotsForDoctor(1L, appointmentDate));
            }

            @Test
            void shouldReturnSlotsWithAvailability() {
                var schedule = DoctorScheduleEntity.builder()
                        .shiftStartTime(LocalTime.of(9, 0))
                        .shiftEndTime(LocalTime.of(10, 0))
                        .isAvailable(true)
                        .build();

                when(doctorScheduleRepository.findByDocIdAndShiftDate(any(), any()))
                        .thenReturn(Optional.of(schedule));
                when(appointmentRepository.findBookedSlotsForDoctor(any(), any()))
                        .thenReturn(List.of());

                var slots = appointmentService.getAvailableSlotsForDoctor(1L, LocalDate.now());
                assertFalse(slots.isEmpty());
            }
        }

        @Nested
        class GetAppointmentsForTodayTests {

            @Test
            void shouldThrowIfDoctorNotFound() {
                when(doctorRepository.findById(1L)).thenReturn(Optional.empty());
                assertThrows(ClinicException.class, () -> appointmentService.getAppointmentsForToday(1L, 0, 10));
            }

            @Test
            void shouldThrowIfNoAppointments() {
                when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
                Page<Appointment> emptyPage = Page.empty();
                when(appointmentRepository.findByDoctorAndDate(any(), any(), any()))
                        .thenReturn(emptyPage);
                assertThrows(ClinicResponseException.class,
                        () -> appointmentService.getAppointmentsForToday(1L, 0, 10));
            }

            @Test
            void shouldReturnAppointments() {
                when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
                patient.setPatientDateOfBirth(LocalDate.of(1990, 8, 16));
                Appointment appointment = Appointment.builder()
                        .patient(patient)
                        .startTime(LocalTime.of(10, 0))
                        .reasonForVisit("Checkup")
                        .build();

                Page<Appointment> appointmentPage = new PageImpl<>(List.of(appointment));
                when(appointmentRepository.findByDoctorAndDate(any(), any(), any()))
                        .thenReturn(appointmentPage);

                var res = appointmentService.getAppointmentsForToday(1L, 0, 10);
                assertEquals(1, res.size());
            }
        }

        @Nested
        class CancelAppointmentTests {

            @Test
            void shouldThrowIfAppointmentNotFound() {
                when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());
                assertThrows(ClinicException.class, () -> appointmentService.cancelAppointment(1L, "reason"));
            }

            @Test
            void shouldReturnErrorIfAlreadyCanceled() {
                Appointment app = Appointment.builder()
                        .appointmentStatus(AppointmentStatus.CANCELED)
                        .build();
                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(app));

                var res = appointmentService.cancelAppointment(1L, "reason");
                assertEquals(ResponseStatus.ERROR, res.getStatus());
            }

            @Test
            void shouldCancelSuccessfully() {
                Appointment app = Appointment.builder()
                        .appointmentStatus(AppointmentStatus.BOOKED)
                        .patient(patient)
                        .doctor(doctor)
                        .appointmentDate(LocalDate.now())
                        .startTime(LocalTime.of(10, 0))
                        .endTime(LocalTime.of(10, 30))
                        .build();

                when(appointmentRepository.findById(1L)).thenReturn(Optional.of(app));
                when(userUtils.getLoggedInUserFullName()).thenReturn("Admin");

                var res = appointmentService.cancelAppointment(1L, "reason");
                assertEquals(ResponseStatus.SUCCESS, res.getStatus());
                verify(notificationService).sendNotification(any());
            }
        }

        @Nested
        class RescheduleAppointmentTests {

            @Test
            void shouldThrowIfAppointmentNotFound() {
                when(appointmentRepository.findById(any())).thenReturn(Optional.empty());
                RescheduleRequestDto dto = new RescheduleRequestDto();

                assertThrows(ClinicException.class, () -> appointmentService.rescheduleAppointment(dto));
            }

            @Test
            void shouldReturnErrorIfAlreadyRescheduled() {
                Appointment app = Appointment.builder()
                        .appointmentStatus(AppointmentStatus.RESCHEDULED)
                        .build();
                when(appointmentRepository.findById(any())).thenReturn(Optional.of(app));

                var res = appointmentService.rescheduleAppointment(RescheduleRequestDto.builder()
                        .appointmentId(1L)
                        .rescheduleAppointmentDate(LocalDate.now())
                        .build());

                assertEquals(ResponseStatus.ERROR, res.getStatus());
            }

            @Test
            void shouldReturnErrorIfDoctorUnavailable() {
                Appointment app = createValidAppointment();
                when(appointmentRepository.findById(any())).thenReturn(Optional.of(app));
                when(doctorScheduleRepository.findByDocIdAndShiftDate(any(), any()))
                        .thenReturn(Optional.of(DoctorScheduleEntity.builder().isAvailable(false).build()));

                var res = appointmentService.rescheduleAppointment(createValidRescheduleDto());
                assertEquals(ResponseStatus.ERROR, res.getStatus());
            }

            @Test
            void shouldReturnErrorIfSlotBooked() {
                Appointment app = createValidAppointment();
                when(appointmentRepository.findById(any())).thenReturn(Optional.of(app));
                when(doctorScheduleRepository.findByDocIdAndShiftDate(any(), any()))
                        .thenReturn(Optional.of(DoctorScheduleEntity.builder().isAvailable(true).build()));
                when(appointmentRepository.findBookedSlotsForDoctor(any(), any()))
                        .thenReturn(List.of(Appointment.builder().appointmentStatus(AppointmentStatus.BOOKED).appointmentDate(LocalDate.now()).doctor(doctor).patient(patient).startTime(LocalTime.of(11, 0))
                                .endTime(LocalTime.of(11, 30)) .build()));

                var res = appointmentService.rescheduleAppointment(createValidRescheduleDto());
                assertEquals(ResponseStatus.ERROR, res.getStatus());
            }

            @Test
            void shouldRescheduleSuccessfully() {
                Appointment app = createValidAppointment();
                when(appointmentRepository.findById(any())).thenReturn(Optional.of(app));
                when(doctorScheduleRepository.findByDocIdAndShiftDate(any(), any()))
                        .thenReturn(Optional.of(DoctorScheduleEntity.builder().isAvailable(true).build()));
                when(appointmentRepository.findBookedSlotsForDoctor(any(), any())).thenReturn(List.of());

                var res = appointmentService.rescheduleAppointment(createValidRescheduleDto());
                assertEquals(ResponseStatus.SUCCESS, res.getStatus());
                verify(notificationService).sendNotification(any());
            }

            @Test
            void shouldThrowClinicResponseExceptionWhenUnexpectedErrorOccurs() {
                // Arrange
                RescheduleRequestDto dto = createValidRescheduleDto();
                when(appointmentRepository.findById(any()))
                        .thenThrow(new RuntimeException("DB connection lost"));

                ClinicResponseException ex = assertThrows(ClinicResponseException.class,
                        () -> appointmentService.rescheduleAppointment(dto));

                assertTrue(ex.getMessage().contains("Error occurred while rescheduling appointment"));
            }

            @Test
            void shouldThrowClinicExceptionWhenRescheduleDateIsAfterMaxAllowedDate() {
                RescheduleRequestDto dto = createValidRescheduleDto();
                dto.setRescheduleAppointmentDate(LocalDate.now().plusDays(3));

                Appointment existingAppointment = createValidAppointment();
                when(appointmentRepository.findById(any())).thenReturn(Optional.of(existingAppointment));

                ClinicException ex = assertThrows(ClinicException.class,
                        () -> appointmentService.rescheduleAppointment(dto));

                assertTrue(ex.getMessage().contains("The booking date needs to be today"));
                assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
            }

            private Appointment createValidAppointment() {
                return Appointment.builder()
                        .appointmentStatus(AppointmentStatus.BOOKED)
                        .patient(patient)
                        .doctor(doctor)
                        .reasonForVisit("Checkup")
                        .appointmentDate(LocalDate.now())
                        .build();
            }

            private RescheduleRequestDto createValidRescheduleDto() {
                return RescheduleRequestDto.builder()
                        .appointmentId(1L)
                        .rescheduleAppointmentDate(LocalDate.now())
                        .appointmentStartTime(LocalTime.of(11, 0))
                        .appointmentEndTime(LocalTime.of(11, 30))
                        .rescheduleReason("Change time")
                        .build();
            }
        }

        @Nested
        class SendNotificationReminderForTodayAppointmentsTests {

            @Test
            void shouldSendReminders() {
                Appointment app = Appointment.builder()
                        .patient(patient)
                        .doctor(doctor)
                        .startTime(LocalTime.of(10, 0))
                        .build();

                when(appointmentRepository.findAllAppointmentsForToday(any())).thenReturn(List.of(app));

                appointmentService.sendNotificationReminderForTodayAppointments();
                verify(notificationService).sendNotification(any());
            }

            @Test
            void shouldLogIfNoAppointments() {
                when(appointmentRepository.findAllAppointmentsForToday(any())).thenReturn(List.of());
                appointmentService.sendNotificationReminderForTodayAppointments();
                verify(notificationService, never()).sendNotification(any());
            }
        }
}

