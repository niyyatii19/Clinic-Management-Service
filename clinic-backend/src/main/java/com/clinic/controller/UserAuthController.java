package com.clinic.controller;

import com.clinic.dto.AuthDto.*;
import com.clinic.models.User;
import com.clinic.service.UserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.clinic.utils.Enums.ResponseStatus;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class UserAuthController {

    private final UserAuthService userAuthService;

    @PostMapping(value = "/login")
    @Operation(description = "Login user and display the role", summary = "Login existing user")
    private ResponseEntity<AuthResponse> loginuser(@Valid @RequestBody AuthRequest authRequest){
        var userLogin = userAuthService.loginUser(authRequest);
        return ResponseEntity.ok().body(userLogin);
    }

    @PostMapping(value = "/register")
    @Operation(description = "Login user and display the role", summary = "Register a new user")
    private ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest){
        var userRegistration = userAuthService.registerUser(registrationRequest);
        return ResponseEntity.ok().body(userRegistration);
    }

    @PostMapping("/refresh")
    @Operation(description = "Refresh JWT token to extend user session", summary = "Refresh authentication token")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request) {
        try {
            String token = userAuthService.extractTokenFromRequest(request);
            return userAuthService.refreshToken(token);
        } catch (Exception e) {
            AuthResponse errorResponse = AuthResponse.builder()
                    .token(null)
                    .role(null)
                    .message("Token refresh failed: " + e.getMessage())
                    .status(ResponseStatus.ERROR)
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(HttpServletRequest request) {
        try {
            String token = userAuthService.extractTokenFromRequest(request);
            User user = userAuthService.getUserProfile(token);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(@RequestBody PasswordChangeRequest request,
                                                       HttpServletRequest httpRequest) {
        try {
            String token = userAuthService.extractTokenFromRequest(httpRequest);
            AuthResponse response = userAuthService.changePassword(token, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            AuthResponse errorResponse = AuthResponse.builder()
                    .token("")
                    .role("")
                    .message("Password change failed")
                    .status(ResponseStatus.ERROR)
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

//    @PostMapping("/logout")
//    public ResponseEntity<AuthResponse> logout(HttpServletRequest request) {
//        try {
//            String token = userAuthService.extractTokenFromRequest(request);
//            userAuthService.logout(token);
//            AuthResponse response = AuthResponse.builder()
//                    .token("")
//                    .role("")
//                    .message("Logout successful")
//                    .status(ResponseStatus.SUCCESS)
//                    .build();
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            AuthResponse errorResponse = AuthResponse.builder()
//                    .token("")
//                    .role("")
//                    .message("Logout failed")
//                    .status(ResponseStatus.ERROR)
//                    .build();
//            return ResponseEntity.badRequest().body(errorResponse);
//        }
//    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<EmailCheckResponse> checkEmail(@PathVariable String email) {
        var exists = userAuthService.emailExists(email);
        return ResponseEntity.ok(new EmailCheckResponse(exists));
    }

    @GetMapping("/check-username/{username}")
    public ResponseEntity<UsernameCheckResponse> checkUsername(@PathVariable String username) {
        boolean exists = userAuthService.usernameExists(username);
        return ResponseEntity.ok(new UsernameCheckResponse(exists));
    }
}
