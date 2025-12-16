package com.pingpong.controller;

import com.pingpong.dto.*;
import com.pingpong.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            // If token is null, it means verification email was resent for existing unverified user
            if (response.getToken() == null) {
                return ResponseEntity.ok(Map.of(
                    "message", "Verification email sent. Please check your inbox.",
                    "username", response.getUsername(),
                    "email", response.getEmail()
                ));
            }
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/send-verification")
    public ResponseEntity<Map<String, String>> sendVerification(@Valid @RequestBody SendVerificationRequest request) {
        try {
            authService.sendVerificationEmail(request.getEmail());
            return ResponseEntity.ok(Map.of("message", "Verification email sent"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        try {
            authService.verifyEmail(request.getEmail(), request.getCode());
            return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmailByCode(@RequestParam String code) {
        try {
            authService.verifyEmailByCode(code);
            return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        // Always return success to not reveal if email exists
        authService.sendPasswordResetEmail(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "If an account exists with this email, a reset code has been sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
