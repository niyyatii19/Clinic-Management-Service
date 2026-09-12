package com.clinic.repository;

import com.clinic.models.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query(value = """
            SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END
                FROM t_appointment ta
                WHERE ta.doc_appointment_id = :doctorId
                  AND ta.appointment_date = :appointmentDate
                  AND ta.appointment_status in ('BOOKED', 'IN_PROGRESS', 'COMPLETED')
                  AND (
                        (:startTime < ta.appointment_end_time AND :endTime > ta.appointment_start_time)
                      )
            """, nativeQuery = true)
    boolean existsByDoctorIdAndAppointmentDateAndTimeOverlap(
            Long doctorId, LocalDate appointmentDate, LocalTime startTime, LocalTime endTime);

    @Query(value = """
           select * from t_appointment ta where  ta.doc_appointment_id = :doctorId and ta.appointment_date = :appointmentDate
                  AND ta.appointment_status in ('BOOKED', 'IN_PROGRESS', 'COMPLETED', 'RESCHEDULED', 'CANCELED')
           """, nativeQuery = true)
    List<Appointment> findBookedSlotsForDoctor(@Param("doctorId") Long doctorId, @Param("appointmentDate") LocalDate appointmentDate);

    @Query(value = """
            select ta.* from t_appointment ta
            where ta.doc_appointment_id = :docId
            and ta.appointment_date = :date
            """, nativeQuery = true)
    Page<Appointment> findByDoctorAndDate(@Param("docId") Long docId, @Param("date") LocalDate todayDate, Pageable pageable);

    @Query(value = """
            select ta.* from t_appointment where ta.appointment_date = :date
            """, nativeQuery = true)
    List<Appointment> findAllAppointmentsForToday(@Param("date") LocalDate today);
}
