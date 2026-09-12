package com.clinic.service.impl;

import com.clinic.Exception.ClinicException;
import com.clinic.Exception.ClinicResponseException;
import com.clinic.dto.AppointmentDTOs.AppointmentRequestDto;
import com.clinic.dto.AppointmentDTOs.DoctorsAppointmentDto;
import com.clinic.dto.AppointmentDTOs.RescheduleRequestDto;
import com.clinic.dto.AppointmentDTOs.TimeSlotDto;
import com.clinic.dto.NotificationMsgDto;
import com.clinic.dto.ResponseDto;
import com.clinic.models.Appointment;
import com.clinic.models.Doctor;
import com.clinic.models.DoctorScheduleEntity;
import com.clinic.models.Patient;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.DoctorScheduleRepository;
import com.clinic.repository.PatientRepository;
import com.clinic.utils.Enums.AppointmentStatus;
import com.clinic.utils.Enums.NotificationEventType;
import com.clinic.utils.Enums.ResponseStatus;
import com.clinic.utils.UtilClasses.CustomUserDetails;
import com.clinic.utils.UtilClasses.TimeRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppointmentServiceImpl implements com.clinic.service.AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final CustomUserDetails userUtils;
    private final NotificationService notificationService;
    private static final String PATIENT_NOT_FOUND = "Patient with id not found : %d";
    private static final String DOCTOR_NOT_FOUND = "Doctor with id not found : %d";
    private static final Duration SLOT_MAX_DURATION_MINUTES = Duration.ofMinutes(30);
    private static final LocalTime START_TIME = LocalTime.of(9, 0);
    private static final LocalTime END_TIME = LocalTime.of(20, 0);


    @Override
    public ResponseDto bookAppointment(AppointmentRequestDto appointmentRequestDto) {
        var today = LocalDate.now();
        var maxAllowedDate = today.plusDays(2);
        if(appointmentRequestDto.getAppointmentDate().isBefore(today) || appointmentRequestDto.getAppointmentDate().isAfter(maxAllowedDate)){
            throw new ClinicException(String.format("The booking date needs to be today %s or 2 days from today till %s", today, maxAllowedDate), HttpStatus.BAD_REQUEST);
        }
        try{
            var patient = patientRepository.findById(appointmentRequestDto.getPatientId())
                    .orElseThrow(() -> new ClinicException(String.format(PATIENT_NOT_FOUND, appointmentRequestDto.getPatientId()), HttpStatus.NOT_FOUND));
            var doctor = doctorRepository.findById(appointmentRequestDto.getDoctorId())
                    .orElseThrow(() -> new ClinicException(String.format(DOCTOR_NOT_FOUND, appointmentRequestDto.getDoctorId()), HttpStatus.NOT_FOUND));
            if (doctor.getSchedules().stream().noneMatch(DoctorScheduleEntity::getIsAvailable)) {
                return ResponseDto.builder()
                        .responseMsg("Doctor is not available at the moment pls choose a different slot.")
                        .status(ResponseStatus.ERROR).build();
            }
            var appointmentStartTime = appointmentRequestDto.getAppointmentStartTime();
            var appointmentEndTime = appointmentRequestDto.getAppointmentEndTime();
            if (appointmentEndTime.isBefore(appointmentStartTime)) {
                appointmentEndTime = appointmentEndTime.plusHours(12);
            }
            if(appointmentStartTime.isBefore(START_TIME) || appointmentEndTime.isAfter(END_TIME)){
                throw new ClinicException("Selected slot is outside working hours", HttpStatus.BAD_REQUEST);
            }
            var duration = Duration.between(appointmentStartTime, appointmentEndTime).toMinutes();
            if(duration > SLOT_MAX_DURATION_MINUTES.toMinutes()){
                throw new ClinicException("Slot duration exceeds the maximum time of the appointment. Please choose a lesser time slot", HttpStatus.BAD_REQUEST);
            }
            var isBooked = appointmentRepository.existsByDoctorIdAndAppointmentDateAndTimeOverlap(appointmentRequestDto.getDoctorId(),
                    appointmentRequestDto.getAppointmentDate(), appointmentStartTime, appointmentEndTime);
            if(isBooked){
                return ResponseDto.builder()
                        .responseMsg("Slot is already booked, please choose a different slot")
                        .status(ResponseStatus.ERROR).build();
            }
            saveEntitiesWithBookingDetails(appointmentRequestDto, appointmentStartTime, appointmentEndTime, doctor, patient);
            var notification = NotificationMsgDto.builder()
                .email(patient.getUser().getEmail())
                .contactNumber(patient.getNumber())
                .subject("Appointment Booked")
                .message(String.format("Your appointment with Dr. %s is booked for %s from %s to %s.", doctor.getDoctorName(), appointmentRequestDto.getAppointmentDate(), appointmentStartTime, appointmentEndTime))
                .type(NotificationEventType.APPOINTMENT_CONFIRMED)
                .build();
            notificationService.sendNotification(notification);
            return ResponseDto.builder()
                    .responseMsg(String.format("Slot is successfully booked for date: %s and slot %s to %s", appointmentRequestDto.getAppointmentDate(), appointmentStartTime, appointmentEndTime))
                    .status(ResponseStatus.SUCCESS).build();
        } catch(ClinicException ex){
            throw ex;
        }
        catch (Exception e){
            throw new ClinicException("Error Occurred when booking an appointment", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ArrayList<TimeSlotDto> getAvailableSlotsForDoctor(Long doctorId, LocalDate appointmentDate) {
        var timeSlots = new ArrayList<TimeSlotDto>();
        var today = LocalDate.now();
        var maxAllowedDate = today.plusDays(2);
        if (appointmentDate.isBefore(today) || appointmentDate.isAfter(maxAllowedDate)) {
            throw new ClinicException(String.format("The appointment date needs to be today %s or 2 days from today till %s", today, maxAllowedDate), HttpStatus.BAD_REQUEST);
        }
        try {
            var docSchedules = doctorScheduleRepository.findByDocIdAndShiftDate(doctorId, appointmentDate)
                    .orElseThrow(() -> new ClinicException(String.format("No doctor found with schedule for appointment date: %s", appointmentDate), HttpStatus.NOT_FOUND));
            var shiftStartTime = parseTo24HourFormat(docSchedules.getShiftStartTime());
            var shiftEndTime = parseTo24HourFormat(docSchedules.getShiftEndTime());
            if (Boolean.FALSE.equals(docSchedules.getIsAvailable())) {
                throw new ClinicResponseException("Doctor is not available at the moment pls choose a different slot.");
            }
            var bookedSlots = appointmentRepository.findBookedSlotsForDoctor(doctorId, appointmentDate);
            var bookedRange = bookedSlots.stream()
                    .map(app -> new TimeRange(app.getStartTime(), app.getEndTime()))
                    .toList();
            var slot = shiftStartTime;
            while (!slot.isAfter(shiftEndTime.minusMinutes(15))) {
                for (int duration : List.of(15, 30)) {
                    LocalTime slotEnd = slot.plusMinutes(duration);

                    if (slotEnd.isAfter(shiftEndTime)) continue;

                    boolean isAvailable = isSlotAvailable(slot, slotEnd, bookedRange);

                    timeSlots.add(TimeSlotDto.builder()
                            .slotStartTime(slot)
                            .slotEndTime(slotEnd)
                            .isAvailable(isAvailable)
                            .build());
                }
                slot = slot.plusMinutes(15);
            }
            return timeSlots;
        } catch (Exception e) {
            throw new ClinicResponseException("Error Occurred when fetching available slots for doctors");
        }
    }

    @Override
    public List<DoctorsAppointmentDto> getAppointmentsForToday(Long doctorId,int page, int size) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ClinicException(String.format("Doctor not found with id: %d", doctorId), HttpStatus.NOT_FOUND));
        var today = LocalDate.now();
        try{
            var pageable = PageRequest.of(page, size);
            var appointments = appointmentRepository.findByDoctorAndDate(doctor.getId(), today, pageable);
            if(appointments.isEmpty()){
                throw new ClinicResponseException(String.format("No appointments found for the doctor with id: %d", doctorId));
            }

            return appointments.stream()
                    .map(docAppointment -> DoctorsAppointmentDto.builder()
                            .patientName(docAppointment.getPatient().getPatientName())
                            .time(docAppointment.getStartTime())
                            .dob(docAppointment.getPatient().getPatientDateOfBirth())
                            .reason(docAppointment.getReasonForVisit())
                            .age(calculateAge(docAppointment.getPatient().getPatientDateOfBirth()))
                            .build()).toList();
        } catch (ClinicException ex){
            throw new ClinicResponseException("Error fetching the appointments for today");
        }
    }

    @Override
    public ResponseDto cancelAppointment(Long appointmentId, String cancelReason) {
        var appointmentToCancel = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ClinicException(String.format("Appointment not found with appointment id: %d", appointmentId), HttpStatus.NOT_FOUND));
        try{
            if(appointmentToCancel.getAppointmentStatus().equals(AppointmentStatus.CANCELED)){
                return ResponseDto.builder().responseMsg("Appointment already canceled").status(ResponseStatus.ERROR).build();
            }
            String updatedBy = userUtils.getLoggedInUserFullName();
            appointmentToCancel.setUpdatedBy(updatedBy);
            appointmentToCancel.setAppointmentStatus(AppointmentStatus.CANCELED);
            appointmentToCancel.setUpdateReason(cancelReason);
            appointmentToCancel.setUpdatedBy(updatedBy);
            appointmentRepository.save(appointmentToCancel);
            var patientEmail = appointmentToCancel.getPatient().getUser().getEmail();
            var patientContact = appointmentToCancel.getPatient().getNumber();
            var doctor = appointmentToCancel.getDoctor();
            var notification = NotificationMsgDto.builder()
                    .email(patientEmail)
                    .contactNumber(patientContact)
                    .subject("Appointment Canceled")
                    .message(String.format("Your appointment with Dr. %s is canceled for %s from %s to %s.", doctor.getDoctorName(), appointmentToCancel.getAppointmentDate(), appointmentToCancel.getStartTime(), appointmentToCancel.getEndTime()))
                    .type(NotificationEventType.APPOINTMENT_CANCELED)
                    .build();
            notificationService.sendNotification(notification);
            return ResponseDto.builder().responseMsg("Appointment canceled successfully").status(ResponseStatus.SUCCESS).build();
        } catch (Exception e){
            throw new ClinicResponseException("Error occurred when cancelling the appointment");
        }
    }

    @Override
    public ResponseDto rescheduleAppointment(RescheduleRequestDto rescheduleRequestDto) {
        try {
            var existingAppointment = appointmentRepository.findById(rescheduleRequestDto.getAppointmentId())
                    .orElseThrow(() -> new ClinicException(
                            String.format("No appointment found with ID: %d", rescheduleRequestDto.getAppointmentId()),
                            HttpStatus.NOT_FOUND));

            var today = LocalDate.now();
            var maxAllowedDate = today.plusDays(2);
            if(rescheduleRequestDto.getRescheduleAppointmentDate().isBefore(today) || rescheduleRequestDto.getRescheduleAppointmentDate().isAfter(maxAllowedDate)){
                throw new ClinicException(String.format("The booking date needs to be today %s or 2 days from today till %s", today, maxAllowedDate), HttpStatus.BAD_REQUEST);
            }
            if(existingAppointment.getAppointmentStatus().equals(AppointmentStatus.RESCHEDULED)){
                return ResponseDto.builder().responseMsg("Appointment already rescheduled").status(ResponseStatus.ERROR).build();
            }
            existingAppointment.setAppointmentStatus(AppointmentStatus.RESCHEDULED);
            existingAppointment.setUpdatedBy(userUtils.getLoggedInUserFullName());
            existingAppointment.setUpdateReason(rescheduleRequestDto.getRescheduleReason());
            appointmentRepository.save(existingAppointment);

            var doctor = existingAppointment.getDoctor();
            var newAppointmentDate = rescheduleRequestDto.getRescheduleAppointmentDate();
            var shift = doctorScheduleRepository.findByDocIdAndShiftDate(doctor.getId(), newAppointmentDate)
                    .orElseThrow(() -> new ClinicException(
                            String.format("Doctor has no schedule on %s", newAppointmentDate),
                            HttpStatus.BAD_REQUEST));

            if (Boolean.FALSE.equals(shift.getIsAvailable())) {
                return ResponseDto.builder()
                        .responseMsg("Doctor is not available on the selected date")
                        .status(ResponseStatus.ERROR)
                        .build();
            }

            var bookedSlots = appointmentRepository.findBookedSlotsForDoctor(doctor.getId(), newAppointmentDate);
            var bookedRange = bookedSlots.stream()
                    .map(app -> new TimeRange(app.getStartTime(), app.getEndTime()))
                    .toList();
            var startTime = rescheduleRequestDto.getAppointmentStartTime();
            var endTime = rescheduleRequestDto.getAppointmentEndTime();

            if (endTime.isBefore(startTime)) {
                endTime = endTime.plusHours(12);
            }
            boolean isSlotFree = isSlotAvailable(startTime, endTime, bookedRange);

            if (!isSlotFree) {
                return ResponseDto.builder()
                        .responseMsg("The selected time slot is already booked. Please choose another.")
                        .status(ResponseStatus.ERROR)
                        .build();
            }

            var newAppointment = Appointment.builder()
                    .doctor(doctor)
                    .patient(existingAppointment.getPatient())
                    .appointmentDate(newAppointmentDate)
                    .startTime(startTime)
                    .endTime(endTime)
                    .reasonForVisit(existingAppointment.getReasonForVisit())
                    .appointmentStatus(AppointmentStatus.BOOKED)
                    .build();

            appointmentRepository.save(newAppointment);

            var patient = existingAppointment.getPatient();
            var notification = NotificationMsgDto.builder()
                    .email(patient.getUser().getEmail())
                    .contactNumber(patient.getNumber())
                    .subject("Appointment Rescheduled")
                    .message(String.format("Your appointment for Dr. %s and for appointment date %s has been rescheduled from %s to %s", doctor.getDoctorName(), existingAppointment.getAppointmentDate(), newAppointment.getStartTime(), newAppointment.getEndTime()))
                    .type(NotificationEventType.APPOINTMENT_RESCHEDULED)
                    .build();
            notificationService.sendNotification(notification);
            return ResponseDto.builder()
                    .responseMsg("Appointment rescheduled successfully")
                    .status(ResponseStatus.SUCCESS)
                    .build();

        } catch (ClinicException e) {
            throw e;
        } catch (Exception e) {
            throw new ClinicResponseException(String.format("Error occurred while rescheduling appointment: %s", e.getMessage()));
        }
    }

    @Override
    public void sendNotificationReminderForTodayAppointments(){
        LocalDate today = LocalDate.now();
        log.info("Fetching appointments for date: {}", today);

        var appointmentsForToday = appointmentRepository.findAllAppointmentsForToday(today);
        if(appointmentsForToday.isEmpty()){
            log.info("No appointments found for today: {}", today);
        }

        appointmentsForToday.forEach(appointment -> {
                var patient = appointment.getPatient();
                var message  = String.format("Dear %s, this is a reminder for your appointment today at Smart Clinic with Dr. %s and appointment time: %s. Please report to the clinic half an hour before your appointment time", patient.getPatientName(), appointment.getDoctor().getDoctorName(), appointment.getStartTime());

                var notification = NotificationMsgDto.builder()
                        .email(patient.getUser().getEmail())
                        .type(NotificationEventType.APPOINTMENT_REMINDER)
                        .subject(String.format("Appointment Reminder for %s", today))
                        .contactNumber(patient.getNumber())
                        .message(message)
                        .build();
                notificationService.sendNotification(notification);
        });

    }

    private void saveEntitiesWithBookingDetails(AppointmentRequestDto appointmentRequestDto, LocalTime appointmentStartTime, LocalTime appointmentEndTime, Doctor doctor, Patient patient){
        var appointment = Appointment.builder()
                .appointmentStatus(AppointmentStatus.BOOKED)
                .doctor(doctor)
                .patient(patient)
                .reasonForVisit(appointmentRequestDto.getReasonForVisit())
                .appointmentDate(appointmentRequestDto.getAppointmentDate())
                .startTime(appointmentStartTime)
                .endTime(appointmentEndTime)
                .build();
        appointmentRepository.save(appointment);
    }

    private boolean isSlotAvailable(LocalTime start, LocalTime end, List<TimeRange> bookedRanges) {
        return bookedRanges.stream().noneMatch(range -> range.overlaps(start, end));
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

    private LocalTime parseTo24HourFormat(LocalTime time) {
        return time;
    }
}
