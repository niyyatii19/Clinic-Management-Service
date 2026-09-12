package com.clinic.service;


import com.clinic.dto.AppointmentDTOs.AppointmentRequestDto;
import com.clinic.dto.AppointmentDTOs.DoctorsAppointmentDto;
import com.clinic.dto.AppointmentDTOs.RescheduleRequestDto;
import com.clinic.dto.AppointmentDTOs.TimeSlotDto;
import com.clinic.dto.ResponseDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public interface AppointmentService {

    ResponseDto bookAppointment(AppointmentRequestDto appointmentRequestDto);
    ArrayList<TimeSlotDto> getAvailableSlotsForDoctor(Long doctorId, LocalDate appointmentDate);
    List<DoctorsAppointmentDto> getAppointmentsForToday(Long doctorId, int page, int size);
    ResponseDto cancelAppointment(Long appointmentId, String cancelReason);
    ResponseDto rescheduleAppointment(RescheduleRequestDto rescheduleRequestDto);
    void sendNotificationReminderForTodayAppointments();
    
}
