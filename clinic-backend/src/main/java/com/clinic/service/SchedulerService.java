package com.clinic.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class SchedulerService {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    @Scheduled(cron = "0 0 * * * ?")
    public void checkAndSetDoctorAvailabilityStatus(){
        log.info("Scheduler running to update doctor availability");
        doctorService.updateDoctorAvailability();
    }

    @Scheduled(cron = "0 0 6 * * ?")
    public void checkAndSendNotificationsReminderToTodayAppointments(){
        log.info("Scheduler running and sending notification to all the patients having appointment today");
        appointmentService.sendNotificationReminderForTodayAppointments();
    }

}
