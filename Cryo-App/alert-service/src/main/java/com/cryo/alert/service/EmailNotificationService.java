package com.cryo.alert.service;

import com.cryo.alert.dto.FreezerDto;
import com.cryo.alert.dto.FreezerReadingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTemperatureAlert(String email, FreezerDto freezer, FreezerReadingDto reading) {
        try {
            // 1. Determine Status Strings
            String doorStatus = (reading.getDoorOpen() != null && reading.getDoorOpen()) ? "OPEN ⚠️" : "CLOSED";
            String powerStatus = (Boolean.TRUE.equals(reading.getFreezerOn())) ? "ON" : "OFF ❌";

            // 2. Determine Alert Type for Subject & Body
            String subject = "RED ALERT: Freezer Temperature High";
            String warningMsg = "Temperature is outside safe range.";

            if (Boolean.FALSE.equals(reading.getFreezerOn())) {
                subject = "CRITICAL: Power Failure - " + freezer.getName();
                warningMsg = "CRITICAL: Power supply has failed!";
            } else if (Boolean.TRUE.equals(reading.getDoorOpen())) {
                subject = "WARNING: Door Open - " + freezer.getName();
                warningMsg = "WARNING: Freezer door has been left open!";
            }

            // 3. Build Email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(String.format(
                    "%s\n\n" +
                            "Freezer: %s\n" +
                            "Name: %s\n" +
                            "Temperature: %.2f°C\n" +
                            "Power Status: %s\n" +
                            "Door Status: %s\n" +
                            "Time: %s\n\n" +
                            "ACTION REQUIRED: %s Please check immediately.",
                    subject,
                    freezer.getFreezerId(),
                    freezer.getName(),
                    reading.getTemperature(),
                    powerStatus,
                    doorStatus,
                    reading.getTimestamp(),
                    warningMsg
            ));

            mailSender.send(message);
            logger.info("Email alert sent successfully to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send email alert to: {}", email, e);
        }
    }
}