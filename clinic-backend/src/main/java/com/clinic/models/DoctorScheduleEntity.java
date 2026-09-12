package com.clinic.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "t_doctor_schedule")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class DoctorScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", nullable = false)
    private Doctor doctor;

    @Column(name = "shift_start_time")
    private LocalTime shiftStartTime;

    @Column(name = "shift_end_time")
    private LocalTime shiftEndTime;

    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;
}