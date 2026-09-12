package com.clinic.dto.AuthDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {

    private String email;
    private String fullName;
    private LocalDate dateOfBirth;
    private Long number;
    private String username;
    private String role;
    private String password;
}
