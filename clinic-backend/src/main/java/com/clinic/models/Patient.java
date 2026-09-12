package com.clinic.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "t_patient")
@Builder
@Data
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_id")
    private Long id;

    @Column(name = "patient_name", nullable = false)
    private String patientName;

    @Column(name = "contact_number", nullable = false)
    private Long number;

    @Column(name = "patient_dob", nullable = false)
    private LocalDate patientDateOfBirth;

    @Column(name = "emergency_contact_number")
    private Long emergencyContactNumber;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "gender")
    private String gender;

    @ToString.Exclude
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<Appointment> patientAppointments;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    @ToString.Exclude
    private User user;

}
