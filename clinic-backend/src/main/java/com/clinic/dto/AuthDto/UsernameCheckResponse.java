package com.clinic.dto.AuthDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsernameCheckResponse {
    private boolean exists;
}
