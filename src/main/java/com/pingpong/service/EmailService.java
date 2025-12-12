package com.pingpong.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@pingpongchampions.com}")
    private String fromEmail;

    @Value("${app.name:Ping Pong Champions}")
    private String appName;

    public void sendVerificationEmail(String toEmail, String code) {
        String subject = appName + " - Verify Your Email";
        String body = String.format(
                "Welcome to %s!\n\n" +
                        "Your email verification code is: %s\n\n" +
                        "This code will expire in 15 minutes.\n\n" +
                        "If you didn't create an account, please ignore this email.",
                appName, code);

        sendEmail(toEmail, subject, body);
        log.info("Verification email sent to {} with code {}", toEmail, code);
    }

    public void sendPasswordResetEmail(String toEmail, String code) {
        String subject = appName + " - Password Reset";
        String body = String.format(
                "You requested a password reset for your %s account.\n\n" +
                        "Your password reset code is: %s\n\n" +
                        "This code will expire in 15 minutes.\n\n" +
                        "If you didn't request this, please ignore this email.",
                appName, code);

        sendEmail(toEmail, subject, body);
        log.info("Password reset email sent to {} with code {}", toEmail, code);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            // Don't throw - we don't want email failures to break the flow
            // The code is still generated and can be found in logs for testing
        }
    }
}
