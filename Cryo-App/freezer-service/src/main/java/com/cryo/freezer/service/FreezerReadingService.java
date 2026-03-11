package com.cryo.freezer.service;
import com.cryo.freezer.entity.Freezer;
import com.cryo.freezer.entity.FreezerReading;
import com.cryo.freezer.event.FreezerAlertEvent;
import com.cryo.freezer.repository.FreezerReadingRepository;
import com.cryo.freezer.repository.FreezerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FreezerReadingService {
    private static final Logger logger = LoggerFactory.getLogger(FreezerReadingService.class);
    private final FreezerReadingRepository freezerReadingRepository;
    private final FreezerRepository freezerRepository;


    //private final WebClient alertServiceClient;
    private final WebClient authServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    // ✅ MEMORY CACHE: Stores last 60 readings for every freezerL
    // Key: FreezerID, Value: List of temperatures
    private final Map<String, LinkedList<BigDecimal>> rollingWindowCache = new ConcurrentHashMap<>();

    public FreezerReadingService(FreezerReadingRepository freezerReadingRepository,
                                 FreezerRepository freezerRepository,
                                 //@Value("${alert.service.url:http://localhost:8083}") String alertServiceUrl,
                                 @Value("${auth.service.url:http://localhost:8081}") String authServiceUrl, KafkaTemplate<String, Object> kafkaTemplate) {

        this.freezerReadingRepository = freezerReadingRepository;
        this.freezerRepository = freezerRepository;
//      this.alertServiceClient = WebClient.builder().baseUrl(alertServiceUrl).build();
        this.authServiceClient = WebClient.builder().baseUrl(authServiceUrl).build();
        this.kafkaTemplate = kafkaTemplate;
    }

    /*@Transactional
    public FreezerReading saveReading(FreezerReading reading) {
        // 1. Save to Database (Permanent History - 1 sec resolution)
        FreezerReading savedReading = freezerReadingRepository.save(reading);

        // 2. ✅ Update RAM Cache (Instant Average Calculation)
        if (reading.getTemperature() != null) {
            updateRollingAverage(reading.getFreezerId(), reading.getTemperature());
        }

        // 3. Trigger Alert Logic (Instant - Checks RAW values immediately)
        boolean isPowerOff = Boolean.FALSE.equals(savedReading.getFreezerOn());
        boolean isDoorOpen = Boolean.TRUE.equals(savedReading.getDoorOpen());

        // We only trigger alerts if it is flagged as Red Alert or Critical Event
        if (savedReading.isRedAlert() || isPowerOff || isDoorOpen) {
            triggerAlertEvaluation(savedReading);
        }

        return savedReading;
    }*/
    public FreezerReading saveReading(FreezerReading reading) {

        logger.info("Saving freezer reading for freezerId={} poNumber={} ts={}",
                reading.getFreezerId(), reading.getPoNumber(), reading.getTimestamp());

        FreezerReading saved;
        try {
            saved = freezerReadingRepository.save(reading);
        } catch (Exception e) {
            logger.error("Failed to persist freezer reading for freezerId={} poNumber={}",
                    reading.getFreezerId(), reading.getPoNumber(), e);
            return reading;
        }

        // Update rolling average
        if (reading.getTemperature() != null) {
            updateRollingAverage(reading.getFreezerId(), reading.getTemperature());
        }

        // 🔥 PUBLISH EVENT TO KAFKA
        Freezer freezer = freezerRepository.findByFreezerId(reading.getFreezerId())
                .orElse(null);

        if (freezer != null) {

            FreezerAlertEvent event =
                    new FreezerAlertEvent(
                            freezer.getFreezerId(),
                            freezer.getOwnerUserId(),
                            reading.getTemperature(),
                            reading.getFreezerOn(),
                            reading.getDoorOpen(),
                            reading.getTimestamp()
                    );

            try {
                kafkaTemplate.send("freezer-alert-topic", event);
            } catch (Exception e) {
                logger.warn("Failed to publish freezer alert event to Kafka. This will not block saving readings.", e);
            }
        }

        logger.info("Saved freezer reading with id={} for freezerId={}",
                saved.getId(), saved.getFreezerId());

        return saved;
    }


    // ✅ HELPER: Updates the list in memory (Max 60 items)
    private void updateRollingAverage(String freezerId, BigDecimal temp) {
        rollingWindowCache.compute(freezerId, (key, list) -> {
            if (list == null) list = new LinkedList<>();
            list.add(temp);
            // Keep only last 60 seconds
            if (list.size() > 60) {
                list.removeFirst();
            }
            return list;
        });
    }

    // ✅ PUBLIC METHOD: Called by Dashboard to get instant average
    public Double getFastOneMinuteAverage(String freezerId) {
        LinkedList<BigDecimal> list = rollingWindowCache.get(freezerId);

        if (list == null || list.isEmpty()) {
            return null;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal val : list) {
            sum = sum.add(val);
        }

        // Calculate Average: Sum / Count
        return sum.divide(BigDecimal.valueOf(list.size()), 2, RoundingMode.HALF_UP).doubleValue();
    }

    // --- ALERT LOGIC (Existing) ---
   /* private void triggerAlertEvaluation(FreezerReading reading) {
        try {
            Freezer freezer = freezerRepository.findByFreezerId(reading.getFreezerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Freezer", reading.getFreezerId()));

            String ownerUserId = freezer.getOwnerUserId();
            String email = null;
            String mobileNumber = null;
            boolean notifyWhatsapp = true;
            boolean notifySms = false;
            boolean notifyEmail = false;

            try {
                UserProfileDto userProfile = authServiceClient.get()
                        .uri("/auth/users/" + ownerUserId)
                        .retrieve()
                        .bodyToMono(UserProfileDto.class)
                        .block();

                if (userProfile != null) {
                    email = userProfile.getEmail();
                    mobileNumber = userProfile.getMobileNumber();
                    notifyWhatsapp = Boolean.TRUE.equals(userProfile.getNotifyWhatsapp());
                    notifySms = Boolean.TRUE.equals(userProfile.getNotifySms());
                    notifyEmail = Boolean.TRUE.equals(userProfile.getNotifyEmail());
                }
            } catch (Exception e) {
                logger.warn("Could not fetch user profile: {}", e.getMessage());
                return;
            }

            AlertEvaluationRequest request = new AlertEvaluationRequest(
                    freezer.getFreezerId(),
                    ownerUserId,
                    reading.getTemperature(),
                    reading.getFreezerOn(),
                    reading.getDoorOpen(),
                    reading.getTimestamp(),
                    email,
                    mobileNumber,
                    freezer.getName(),
                    notifyWhatsapp,
                    notifySms,
                    notifyEmail
            );

            alertServiceClient.post()
                    .uri("/alerts/evaluate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe(
                            result -> logger.info("Alert triggered for freezer: {}", reading.getFreezerId()),
                            error -> {
                                if (error instanceof WebClientRequestException) {
                                    logger.warn("⚠️ Alert Service is DOWN.");
                                } else {
                                    logger.error("Failed to trigger alert", error);
                                }
                            }
                    );

        } catch (Exception e) {
            logger.error("Error triggering alert evaluation", e);
        }
    }

    private static class AlertEvaluationRequest {
        public String freezerId;
        public String ownerUserId;
        public BigDecimal temperature;
        public Boolean freezerOn;
        public Boolean doorOpen;
        public LocalDateTime timestamp;
        public String email;
        public String mobileNumber;
        public String freezerName;
//        public BigDecimal minThreshold;
//        public BigDecimal maxThreshold;
        public Boolean notifyWhatsapp;
        public Boolean notifySms;
        public Boolean notifyEmail;

        public AlertEvaluationRequest(String freezerId, String ownerUserId, BigDecimal temperature,
                                      Boolean freezerOn, Boolean doorOpen, LocalDateTime timestamp,
                                      String email, String mobileNumber, String freezerName,
                                      Boolean notifyWhatsapp, Boolean notifySms, Boolean notifyEmail) {
            this.freezerId = freezerId;
            this.ownerUserId = ownerUserId;
            this.temperature = temperature;
            this.freezerOn = freezerOn;
            this.doorOpen = doorOpen;
            this.timestamp = timestamp;
            this.email = email;
            this.mobileNumber = mobileNumber;
            this.freezerName = freezerName;
//            this.minThreshold = minThreshold;
//            this.maxThreshold = maxThreshold;
            this.notifyWhatsapp = notifyWhatsapp;
            this.notifySms = notifySms;
            this.notifyEmail = notifyEmail;
        }
        // Getters needed for serialization (Lombok @Data recommended, but explicit here)
        public String getFreezerId() { return freezerId; }
        public String getOwnerUserId() { return ownerUserId; }
        public BigDecimal getTemperature() { return temperature; }
        public Boolean getFreezerOn() { return freezerOn; }
        public Boolean getDoorOpen() { return doorOpen; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getEmail() { return email; }
        public String getMobileNumber() { return mobileNumber; }
        public String getFreezerName() { return freezerName; }
//        public BigDecimal getMinThreshold() { return minThreshold; }
//        public BigDecimal getMaxThreshold() { return maxThreshold; }
        public Boolean getNotifyWhatsapp() { return notifyWhatsapp; }
        public Boolean getNotifySms() { return notifySms; }
        public Boolean getNotifyEmail() { return notifyEmail; }
    }*/
}