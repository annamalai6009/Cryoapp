package com.cryo.alert.service;

import com.cryo.alert.dto.FreezerDto;
import com.cryo.alert.dto.FreezerReadingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Fast2SmsService {

    private static final Logger logger = LoggerFactory.getLogger(Fast2SmsService.class);

    @Value("${sms.fast2sms.api-key}")
    private String apiKey;

    @Value("${sms.fast2sms.route}")
    private String route;

    private final RestTemplate restTemplate;

    public Fast2SmsService() {
        this.restTemplate = new RestTemplate();
    }

    public void sendTemperatureAlert(String mobileNumber, FreezerDto freezer, FreezerReadingDto reading) {
        try {
            if (mobileNumber == null || mobileNumber.trim().isEmpty()) return;

            // Clean number
            String cleanNumber = mobileNumber;
            if (mobileNumber.startsWith("+91")) cleanNumber = mobileNumber.substring(3);
            else if (mobileNumber.startsWith("91") && mobileNumber.length() == 12) cleanNumber = mobileNumber.substring(2);

            // ✅ DYNAMIC MESSAGE GENERATION
            String reason;
            if (Boolean.FALSE.equals(reading.getFreezerOn())) {
                reason = "POWER FAILURE! Power is OFF.";
            } else if (Boolean.TRUE.equals(reading.getDoorOpen())) {
                reason = "DOOR OPEN! Door is not closed.";
            } else {
                reason = String.format("High Temp: %.2f C.", reading.getTemperature());
            }

            String message = String.format("CRITICAL: Freezer %s (%s). %s Check Immediately!",
                    freezer.getFreezerId(),
                    freezer.getName(),
                    reason
            );

            logger.info("Initiating SMS to: {}", cleanNumber);

            String url = "https://www.fast2sms.com/dev/bulkV2?authorization=" + apiKey +
                    "&route=" + route +
                    "&message=" + message +
                    "&language=english" +
                    "&flash=0" +
                    "&numbers=" + cleanNumber;

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            headers.set("cache-control", "no-cache");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", mobileNumber, e.getMessage());
        }
    }
}