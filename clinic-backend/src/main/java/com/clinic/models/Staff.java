package com.clinic.models;

import com.clinic.utils.Enums.StaffPosition;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "t_staff")
@Builder
@Data
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long id;

    @Column(name = "staff_name", nullable = false)
    private String staffName;

    @Column(name = "staff_position")
    @Enumerated(EnumType.STRING)
    private StaffPosition position;

    @Column(name = "contact_number", nullable = false)
    private Long number;

    @Column(name = "staff_dob", nullable = false)
    private LocalDate staffDateOfBirth;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    @ToString.Exclude
    private User user;
}
