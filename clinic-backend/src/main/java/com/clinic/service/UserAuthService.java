package com.clinic.service;

import com.clinic.dto.AuthDto.AuthRequest;
import com.clinic.dto.AuthDto.AuthResponse;
import com.clinic.dto.AuthDto.RegistrationRequest;
import com.clinic.models.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface UserAuthService  {

    AuthResponse registerUser(RegistrationRequest registrationRequest);
    AuthResponse loginUser(AuthRequest authRequest);
    ResponseEntity<AuthResponse> refreshToken(String token);
    String extractTokenFromRequest(HttpServletRequest request);
    User getUserProfile(String token);
    AuthResponse changePassword(String token, com.clinic.dto.AuthDto.PasswordChangeRequest request);
    boolean emailExists(String email);
    boolean usernameExists(String username);
}
