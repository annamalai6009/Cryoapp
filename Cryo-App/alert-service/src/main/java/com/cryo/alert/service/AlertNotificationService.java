package com.cryo.alert.service;

import com.cryo.alert.dto.FreezerDto;
import com.cryo.alert.dto.FreezerReadingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AlertNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(AlertNotificationService.class);

    private final SmsNotificationService smsNotificationService; // For WhatsApp
    private final EmailNotificationService emailNotificationService;
    private final Fast2SmsService fast2SmsService; // For SMS

    public AlertNotificationService(SmsNotificationService smsNotificationService,
                                    EmailNotificationService emailNotificationService,
                                    Fast2SmsService fast2SmsService) {
        this.smsNotificationService = smsNotificationService;
        this.emailNotificationService = emailNotificationService;
        this.fast2SmsService = fast2SmsService;
    }

    @Async
    public void sendAlertNotifications(String mobileNumber, String email,
                                       FreezerDto freezer, FreezerReadingDto reading,
                                       // ✅ Preferences Arguments
                                       Boolean notifyWhatsapp, Boolean notifySms, Boolean notifyEmail) {

        // 1. WhatsApp Logic
        if (Boolean.TRUE.equals(notifyWhatsapp)) {
            try {
                // The reading object inside here has doorOpen/freezerOn flags.
                // Your SmsNotificationService uses them to decide the text.
                smsNotificationService.sendTemperatureAlert(mobileNumber, freezer, reading);
                logger.info("✅ WhatsApp notification sent to: {}", mobileNumber);
            } catch (Exception e) {
                logger.error("❌ Failed to send WhatsApp notification", e);
            }
        }

        // 2. Email Logic
        if (Boolean.TRUE.equals(notifyEmail)) {
            try {
                emailNotificationService.sendTemperatureAlert(email, freezer, reading);
                logger.info("✅ Email notification sent to: {}", email);
            } catch (Exception e) {
                logger.error("❌ Failed to send email notification", e);
            }
        }

        // 3. SMS Logic (Fast2SMS)
        if (Boolean.TRUE.equals(notifySms)) {
            try {
                // Uncomment this when you are ready to pay for SMS
                // fast2SmsService.sendTemperatureAlert(mobileNumber, freezer, reading);
                logger.info("✅ SMS task triggered for: {} (Simulation)", mobileNumber);
            } catch (Exception e) {
                logger.error("❌ Failed to trigger Fast2SMS", e);
            }
        }
    }
}