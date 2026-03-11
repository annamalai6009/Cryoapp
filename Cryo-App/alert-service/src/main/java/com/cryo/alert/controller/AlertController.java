package com.cryo.alert.controller;

import com.cryo.alert.dto.AlertEvaluationRequest;
import com.cryo.alert.dto.FreezerDto;
import com.cryo.alert.dto.FreezerReadingDto;
import com.cryo.alert.entity.Alert;
import com.cryo.alert.repository.AlertRepository;
import com.cryo.alert.service.AlertEvaluationService;
import com.cryo.alert.service.AlertNotificationService;
import com.cryo.alert.repository.FreezerAlertStateRepository;
import com.cryo.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertEvaluationService alertEvaluationService;
    private final AlertNotificationService alertNotificationService;

    @Autowired
    private FreezerAlertStateRepository stateRepository;

    @Autowired
    private AlertRepository alertRepository;

    public AlertController(AlertEvaluationService alertEvaluationService,
                           AlertNotificationService alertNotificationService) {
        this.alertEvaluationService = alertEvaluationService;
        this.alertNotificationService = alertNotificationService;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<Void>> evaluateAlert(@Valid @RequestBody AlertEvaluationRequest request) {

        FreezerReadingDto reading = new FreezerReadingDto(
                request.getFreezerId(),
                request.getTemperature(),
                request.getFreezerOn(),
                request.getDoorOpen(),
                request.getTimestamp()
        );

        FreezerDto freezer = new FreezerDto(
                request.getFreezerId(),
                request.getFreezerName()
        );

        Alert alert = alertEvaluationService.evaluateTemperatureAlert(
                request.getFreezerId(),
                request.getOwnerUserId(),
                request.getTemperature(),
                freezer,
                reading,
                request.getMinThreshold(), // Pass Min
                request.getMaxThreshold()  // Pass Max
        );

        if (alert != null) {
            // ✅ UPDATED CALL: Pass the notification preferences from the request
            alertNotificationService.sendAlertNotifications(
                    request.getMobileNumber(),
                    request.getEmail(),
                    freezer,
                    reading,
                    request.getNotifyWhatsapp(), // ✅ Pass flag
                    request.getNotifySms(),      // ✅ Pass flag
                    request.getNotifyEmail()     // ✅ Pass flag
            );

            alert.setNotificationSent(true);
            alertRepository.save(alert);
        }

        return ResponseEntity.ok(ApiResponse.success("Alert evaluation completed", null));
    }
}