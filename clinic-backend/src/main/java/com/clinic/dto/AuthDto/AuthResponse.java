package com.clinic.dto.AuthDto;

import com.clinic.utils.Enums.ResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AuthResponse {

    private String token;
    private String role;
    private String message;
    private ResponseStatus status;
}
