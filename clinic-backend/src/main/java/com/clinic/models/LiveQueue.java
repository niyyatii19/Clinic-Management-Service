package com.clinic.models;

import com.clinic.utils.Enums.QueueStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "t_live_queue")
@Builder
@Data
public class LiveQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "queue_id")
    private Long id;

    @Column(name = "queue_position", nullable = false)
    private boolean queuePosition;

    @Column(name = "queue_status", nullable = false)
    private QueueStatus queueStatus;

    @OneToOne
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "queue_patient_id", nullable = false)
    private Patient patient;

    @Column(name = "appointment_start_time", nullable = false)
    private LocalDateTime appointmentStartTime;

    @Column(name = "appointment_end_time", nullable = false)
    private LocalDateTime appointmentEndTime;

}

