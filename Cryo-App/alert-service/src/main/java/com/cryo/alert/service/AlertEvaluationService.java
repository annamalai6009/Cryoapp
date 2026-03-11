package com.cryo.alert.service;
import com.cryo.alert.dto.FreezerDto;
import com.cryo.alert.dto.FreezerReadingDto;
import com.cryo.alert.entity.Alert;
import com.cryo.alert.entity.FreezerAlertState;
import com.cryo.alert.repository.AlertRepository;
import com.cryo.alert.repository.FreezerAlertStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AlertEvaluationService {
    private static final Logger logger = LoggerFactory.getLogger(AlertEvaluationService.class);

    private final AlertRepository alertRepository;
    private final FreezerAlertStateRepository stateRepository;

    public AlertEvaluationService(AlertRepository alertRepository,
                                  FreezerAlertStateRepository stateRepository) {
        this.alertRepository = alertRepository;
        this.stateRepository = stateRepository;
    }

    @Transactional
    public Alert evaluateTemperatureAlert(String freezerId,
                                          String ownerUserId,
                                          BigDecimal temperature,
                                          FreezerDto freezer,
                                          FreezerReadingDto reading,
                                          BigDecimal minThreshold,
                                          BigDecimal maxThreshold) {

        // 1. Get current state or create new
        FreezerAlertState state = stateRepository.findById(freezerId)
                .orElse(new FreezerAlertState(freezerId));

        // 2. CHECK CONDITIONS (Priority Order)

        // A. Check Power (Critical) - Assumes getFreezerOn() returns true if power is ON
        boolean isPowerOff = Boolean.FALSE.equals(reading.getFreezerOn());

        // B. Check Door (Warning) - Assumes getDoorOpen() returns true if door is OPEN
        boolean isDoorOpen = Boolean.TRUE.equals(reading.getDoorOpen());

        // C. Check Temperature (Standard)
        boolean isTempUnsafe = temperature.compareTo(minThreshold) < 0 || temperature.compareTo(maxThreshold) > 0;

        // D. Combined Unsafe Flag
        boolean isUnsafe = isPowerOff || isDoorOpen || isTempUnsafe;

        // Determine specific message for logging
        String violationMessage = "Temperature Alert";
        if (isPowerOff) violationMessage = "CRITICAL: Power Failure";
        else if (isDoorOpen) violationMessage = "WARNING: Door Open";

        if (isUnsafe) {
            // --- DANGER ZONE ---

            // A. If User already Acknowledged -> STOP (Silence)
            if (Boolean.TRUE.equals(state.getAcknowledged())) {
                return null;
            }

            // B. If Already Active (and not Acked) -> STOP (Don't spam)
            if (Boolean.TRUE.equals(state.getActive())) {
                return null;
            }

            // C. New Incident -> Trigger Alert!
            state.setActive(true);
            state.setAcknowledged(false); // Reset Ack
            state.setLastAlertTime(LocalDateTime.now());
            stateRepository.save(state);

            // Log Alert to DB
            Alert alert = new Alert();
            alert.setFreezerId(freezerId);
            alert.setOwnerUserId(ownerUserId);
            alert.setTemperature(temperature);

            // Use the generic RED_ALERT type, but the 'reading' object carries the door/power status
            alert.setAlertType(Alert.AlertType.RED_ALERT);

            // Optional: If your Alert entity has a message field, set it here.
            // alert.setMessage(violationMessage);

            alert.setTimestamp(reading.getTimestamp());
            alert.setNotificationSent(false);

            logger.info("🔴 TRIGGER: New Alert for {} | Reason: {}", freezerId, violationMessage);
            return alertRepository.save(alert);

        } else {
            // --- SAFE / RECOVERY ZONE ---
            // We only reach here if Power is ON, Door is CLOSED, and Temp is NORMAL.

            // 3. Dynamic Calculation: Calculate Midpoint (Reset Threshold)
            BigDecimal sum = minThreshold.add(maxThreshold);
            BigDecimal midpoint = sum.divide(new BigDecimal("2"));

            // LOGIC: If we are 'Active' (meaning we had an alert), we check if we recovered.
            // Since we are in the 'else' block, we are theoretically safe.

            if (Boolean.TRUE.equals(state.getActive())) {
                logger.info("🟢 RECOVERY: Freezer {} is safe ({}). Resetting Alert State.", freezerId, temperature);
                state.setActive(false);
                state.setAcknowledged(false);
                stateRepository.save(state);
            }
            return null;
        }
    }
}