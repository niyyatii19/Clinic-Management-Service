package com.clinic.models;

import com.clinic.utils.Enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "t_appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long id;

    @Column(name = "appointment_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AppointmentStatus appointmentStatus;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "appointment_start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "appointment_end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "reason_for_visit")
    private String reasonForVisit;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "update_reason")
    private String updateReason;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "doc_appointment_id")
    private Doctor doctor;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "patient_appointment_id")
    private Patient patient;
}
