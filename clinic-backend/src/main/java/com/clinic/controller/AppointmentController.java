package com.clinic.controller;

import com.clinic.dto.AppointmentDTOs.AppointmentRequestDto;
import com.clinic.dto.AppointmentDTOs.DoctorsAppointmentDto;
import com.clinic.dto.AppointmentDTOs.RescheduleRequestDto;
import com.clinic.dto.AppointmentDTOs.TimeSlotDto;
import com.clinic.dto.ResponseDto;
import com.clinic.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/book")
    @Operation(summary = "Book an appointment")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<ResponseDto> bookAppointment(@RequestBody AppointmentRequestDto request) {
        return ResponseEntity.ok(appointmentService.bookAppointment(request));
    }

    @GetMapping("/available-slots/{doctorId}")
    @Operation(summary = "Get available slots for the next 3 days")
    @PreAuthorize("hasAnyRole('PATIENT', 'STAFF', 'ADMIN')")
    public ResponseEntity<List<TimeSlotDto>> getAvailableSlotsForDoctor(@Valid @PathVariable Long doctorId, LocalDate appointmentDate) {
        return ResponseEntity.ok(appointmentService.getAvailableSlotsForDoctor(doctorId, appointmentDate));
    }

    @GetMapping("/appointments/{docId}")
    @Operation(summary = "Doctor view: get today's appointments")
    @PreAuthorize("hasRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<DoctorsAppointmentDto>> getTodayAppointments(@Valid @PathVariable Long docId, @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForToday(docId, page, size));
    }

    @PutMapping("/cancel/{appointmentId}")
    @Operation(summary = "Cancel an appointment")
    @PreAuthorize("hasAnyRole('PATIENT', 'RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<ResponseDto> cancelAppointment(@Valid @PathVariable Long appointmentId, String cancelReason) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(appointmentId, cancelReason));
    }
    @PutMapping("/reschedule")
    @Operation(summary = "Reschedule an appointment")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<ResponseDto> rescheduleAppointment(@RequestBody RescheduleRequestDto request) {
        return ResponseEntity.ok(appointmentService.rescheduleAppointment(request));
    }
}

