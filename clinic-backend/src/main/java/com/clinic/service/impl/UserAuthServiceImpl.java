package com.clinic.service.impl;

import com.clinic.Exception.ClinicException;
import com.clinic.dto.AuthDto.AuthRequest;
import com.clinic.dto.AuthDto.AuthResponse;
import com.clinic.dto.AuthDto.RegistrationRequest;
import com.clinic.models.Doctor;
import com.clinic.models.Patient;
import com.clinic.models.Staff;
import com.clinic.models.User;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.PatientRepository;
import com.clinic.repository.StaffRepository;
import com.clinic.repository.UserRepository;
import com.clinic.utils.Enums.ResponseStatus;
import com.clinic.utils.Enums.UserRoles;
import com.clinic.utils.UtilClasses.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class UserAuthServiceImpl implements com.clinic.service.UserAuthService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final StaffRepository staffRepository;
    private final JwtUtils jwtUtils;

    @Override
    public AuthResponse registerUser(RegistrationRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail()) || userRepository.existsByUserName(request.getUsername())) {
                throw new ClinicException(String.format("User with email: %s and username: %s already exists in the system", request.getEmail(), request.getUsername()), HttpStatus.BAD_REQUEST);
            }
            var newUser = User.builder().userName(request.getUsername()).email(request.getEmail()).password(passwordEncoder.encode(request.getPassword()))
                    .fullName(request.getFullName())
                    .role(UserRoles.valueOf(request.getRole()))
                    .userDateOfBirth(request.getDateOfBirth())
                    .number(request.getNumber())
                    .build();
            userRepository.save(newUser);

            switch (UserRoles.valueOf(request.getRole().toUpperCase())) {
                case DOCTOR -> {
                    var doc = Doctor.builder()
                            .docDateOfBirth(request.getDateOfBirth())
                            .doctorName(request.getFullName())
                            .number(request.getNumber())
                            .user(newUser)
                            .build();
                    doctorRepository.save(doc);
                }
                case PATIENT -> {
                    var patient = Patient.builder()
                            .patientDateOfBirth(request.getDateOfBirth())
                            .patientName(request.getFullName())
                            .number(request.getNumber())
                            .user(newUser)
                            .build();
                    patientRepository.save(patient);
                }
                case STAFF -> {
                    var staff = Staff.builder()
                            .staffDateOfBirth(request.getDateOfBirth())
                            .number(request.getNumber())
                            .staffName(request.getFullName())
                            .user(newUser)
                            .build();
                    staffRepository.save(staff);
                }
                case ADMIN -> {/* Nothing to perform */}
            }
            String token = jwtUtils.generateToken(newUser.getUserName(), newUser.getRole().name());
            return new AuthResponse(token, newUser.getRole().name(), String.format("User with username: %s successfully registered", newUser.getUserName()), ResponseStatus.SUCCESS);
        } catch (ClinicException e){
            return new AuthResponse(null, null, String.format("Error occurred when registering the user: %s", request.getUsername()), ResponseStatus.ERROR);
        }
    }

    @Override
    public AuthResponse loginUser(AuthRequest authRequest) {
        try {
            var user = userRepository.findByEmail(authRequest.getLoginInput());
            var LoginIdentifierType = "Email";
            if (user.isEmpty()) {
                user = userRepository.findByUserName(authRequest.getLoginInput());
                LoginIdentifierType = "UserName";
            }
            var userLogin = user.orElseThrow(() -> new ClinicException(String.format("User: %s not found in the system do register and try again", authRequest.getLoginInput()), HttpStatus.NOT_FOUND));

            if (!passwordEncoder.matches(authRequest.getPassword(), userLogin.getPassword())) {
                throw new ClinicException(String.format("Login error password is invalid for the user: %s", authRequest.getLoginInput()), HttpStatus.BAD_REQUEST);
            }

            var token = jwtUtils.generateToken(userLogin.getEmail(), userLogin.getRole().name());
            return new AuthResponse(token, userLogin.getRole().name(), String.format("Login successful for the user: %s (%s)", authRequest.getLoginInput(), LoginIdentifierType), ResponseStatus.SUCCESS);
        } catch (ClinicException e) {
            return new AuthResponse(null, null, String.format("User: %s not found in the system do register and try again", authRequest.getLoginInput()), ResponseStatus.ERROR);
        }
    }

    @Override
    public ResponseEntity<AuthResponse> refreshToken(String token) {
        try {
            log.info("Starting token refresh process for token: {}", token != null ? "[PRESENT]" : "[NULL]");
            if (token == null || token.trim().isEmpty()) {
                throw new ClinicException("Token is required for refresh", HttpStatus.BAD_REQUEST);
            }

            if (!jwtUtils.isTokenValid(token)) {
                throw new ClinicException("Token is invalid or expired", HttpStatus.UNAUTHORIZED);
            }

            var userEmail = jwtUtils.extractUsername(token);
            var userRole = jwtUtils.extractRole(token);
            
            log.info("Refreshing token for user: {} with role: {}", userEmail, userRole);

            userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ClinicException("User not found in system", HttpStatus.NOT_FOUND));

            String newToken = jwtUtils.generateToken(userEmail, userRole);
            log.info("Successfully generated new token for user: {}", userEmail);
            AuthResponse response = AuthResponse.builder()
                    .token(newToken)
                    .role(userRole)
                    .message(String.format("Token refreshed successfully for user: %s. New token expires in 10 hours.", userEmail))
                    .status(ResponseStatus.SUCCESS)
                    .build();

            return ResponseEntity.ok(response);
            
        } catch (ClinicException e) {
            log.error("Token refresh failed: {}", e.getMessage());
            AuthResponse errorResponse = AuthResponse.builder()
                    .token(null)
                    .role(null)
                    .message(e.getMessage())
                    .status(ResponseStatus.ERROR)
                    .build();
            return ResponseEntity.status(e.getHttpStatus()).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error during token refresh: {}", e.getMessage(), e);
            AuthResponse errorResponse = AuthResponse.builder()
                    .token(null)
                    .role(null)
                    .message("Token refresh failed due to server error")
                    .status(ResponseStatus.ERROR)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public String extractTokenFromRequest(jakarta.servlet.http.HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }
        throw new ClinicException("Authorization header missing or invalid", HttpStatus.BAD_REQUEST);
    }

    @Override
    public User getUserProfile(String token) {
        if (!jwtUtils.isTokenValid(token)) {
            throw new ClinicException("Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }
        
        String userEmail = jwtUtils.extractUsername(token);
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ClinicException("User not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public AuthResponse changePassword(String token, com.clinic.dto.AuthDto.PasswordChangeRequest request) {
        // Implementation placeholder - would need proper password change logic
        throw new ClinicException("Method not implemented yet", HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean usernameExists(String username) {
        return userRepository.existsByUserName(username);
    }
}
