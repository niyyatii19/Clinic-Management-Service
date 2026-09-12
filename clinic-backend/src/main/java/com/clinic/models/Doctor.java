package com.clinic.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "t_doctor")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_id")
    private Long id;

    @Column(name = "doc_name",nullable = false)
    private String doctorName;

    @Column(name = "doc_dob", nullable = false)
    private LocalDate docDateOfBirth;

    @Column(name = "contact_number", nullable = false)
    private Long number;

    @Column(name = "doc_specialization")
    private String specializations;

    @ToString.Exclude
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DoctorScheduleEntity> schedules;

    @ToString.Exclude
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<Appointment> allAppointments;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    @ToString.Exclude
    private User user;

}
