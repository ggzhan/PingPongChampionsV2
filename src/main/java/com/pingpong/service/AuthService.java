package com.pingpong.service;

import com.pingpong.aspect.RetryOnDbFailure;
import com.pingpong.dto.*;
import com.pingpong.model.User;
import com.pingpong.model.VerificationCode;
import com.pingpong.repository.UserRepository;
import com.pingpong.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@RetryOnDbFailure(maxAttempts = 3, initialDelayMs = 1000, multiplier = 2.0)
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    private static final String TYPE_EMAIL_VERIFY = "EMAIL_VERIFY";
    private static final String TYPE_PASSWORD_RESET = "PASSWORD_RESET";
    private static final int CODE_EXPIRY_MINUTES = 15;

    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmailVerified(false);

        userRepository.save(user);

        // Send verification email automatically on registration
        try {
            sendVerificationEmailInternal(user);
        } catch (Exception e) {
            log.warn("Failed to send verification email on registration: {}", e.getMessage());
        }

        // Generate JWT token
        String jwtToken = jwtService.generateToken(user.getUsername());

        return new AuthResponse(jwtToken, user.getUsername(), user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        // Get user from database
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate JWT token
        String jwtToken = jwtService.generateToken(user.getUsername());

        return new AuthResponse(jwtToken, user.getUsername(), user.getEmail());
    }

    @Transactional
    public void sendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getEmailVerified()) {
            throw new RuntimeException("Email already verified");
        }

        sendVerificationEmailInternal(user);
    }

    private void sendVerificationEmailInternal(User user) {
        // Invalidate any existing codes
        List<VerificationCode> existingCodes = verificationCodeRepository
                .findByUserIdAndTypeAndUsedFalse(user.getId(), TYPE_EMAIL_VERIFY);
        existingCodes.forEach(code -> code.setUsed(true));
        verificationCodeRepository.saveAll(existingCodes);

        // Generate new code
        String code = generateCode();
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setUser(user);
        verificationCode.setCode(code);
        verificationCode.setType(TYPE_EMAIL_VERIFY);
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));
        verificationCodeRepository.save(verificationCode);

        // Send email
        emailService.sendVerificationEmail(user.getEmail(), code);
    }

    @Transactional
    public void verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        VerificationCode verificationCode = verificationCodeRepository
                .findByCodeAndTypeAndUsedFalse(code, TYPE_EMAIL_VERIFY)
                .orElseThrow(() -> new RuntimeException("Invalid verification code"));

        if (!verificationCode.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Invalid verification code");
        }

        if (verificationCode.isExpired()) {
            throw new RuntimeException("Verification code has expired");
        }

        // Mark code as used
        verificationCode.setUsed(true);
        verificationCodeRepository.save(verificationCode);

        // Mark user as verified
        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("Email verified for user: {}", user.getUsername());
    }

    @Transactional
    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);

        // Don't reveal if user exists or not for security
        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }

        // Invalidate any existing codes
        List<VerificationCode> existingCodes = verificationCodeRepository
                .findByUserIdAndTypeAndUsedFalse(user.getId(), TYPE_PASSWORD_RESET);
        existingCodes.forEach(code -> code.setUsed(true));
        verificationCodeRepository.saveAll(existingCodes);

        // Generate new code
        String code = generateCode();
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setUser(user);
        verificationCode.setCode(code);
        verificationCode.setType(TYPE_PASSWORD_RESET);
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));
        verificationCodeRepository.save(verificationCode);

        // Send email
        emailService.sendPasswordResetEmail(user.getEmail(), code);
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid reset code"));

        VerificationCode verificationCode = verificationCodeRepository
                .findByCodeAndTypeAndUsedFalse(code, TYPE_PASSWORD_RESET)
                .orElseThrow(() -> new RuntimeException("Invalid reset code"));

        if (!verificationCode.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Invalid reset code");
        }

        if (verificationCode.isExpired()) {
            throw new RuntimeException("Reset code has expired");
        }

        // Mark code as used
        verificationCode.setUsed(true);
        verificationCodeRepository.save(verificationCode);

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password reset for user: {}", user.getUsername());
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // 6-digit code
        return String.valueOf(code);
    }
}
