package com.cryo.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Freezer Monitoring - OTP Verification");
            message.setText(String.format(
                    "Your OTP for account verification is: %s\n\n" +
                            "This OTP will expire in 10 minutes.\n\n" +
                            "If you did not request this OTP, please ignore this email.",
                    otpCode));

            mailSender.send(message);
            logger.info("OTP email sent to: {}", toEmail);
        } catch (Exception e) {
            // Email delivery should not make signup/login fail (it can be retried via resend-otp).
            logger.error("Failed to send OTP email to: {}. User can use resend OTP. Error: {}", toEmail, e.getMessage());
        }
    }
}

