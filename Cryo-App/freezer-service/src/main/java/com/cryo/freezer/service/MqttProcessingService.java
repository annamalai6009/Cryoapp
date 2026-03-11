//package com.cryo.freezer.service;
//
//import com.cryo.freezer.dto.S3Payload;
//import com.cryo.freezer.entity.Freezer;
//import com.cryo.freezer.entity.FreezerReading;
//import com.cryo.freezer.repository.FreezerReadingRepository;
//import com.cryo.freezer.repository.FreezerRepository;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.MapperFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.integration.annotation.ServiceActivator;
//import org.springframework.messaging.MessageHandler;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Optional;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Service
//public class MqttProcessingService {
//
//    private static final Logger log = LoggerFactory.getLogger(MqttProcessingService.class);
//
//    private final FreezerReadingService freezerReadingService;
//    private final FreezerRepository freezerRepository;
//    private final FreezerReadingRepository readingRepository;
//    private final ObjectMapper mapper;
//
//    // Regex to detect "DL" vs "Cryo"
//    private static final Pattern TOPIC_PREFIX_PATTERN = Pattern.compile("^([A-Za-z]+)");
//    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
//    public MqttProcessingService(FreezerReadingService freezerReadingService,
//                                 FreezerRepository freezerRepository,
//                                 FreezerReadingRepository readingRepository) {
//        this.freezerReadingService = freezerReadingService;
//        this.freezerRepository = freezerRepository;
//        this.readingRepository = readingRepository;
//        // Config Mapper to ignore extra fields safely
//        this.mapper = new ObjectMapper()
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true); // <--- ADD THIS LINE
//
//    }
//
//    // ✅ THE HANDLER: This runs AUTOMATICALLY when data arrives
////    @Bean
////    @ServiceActivator(inputChannel = "mqttInputChannel")
////    public MessageHandler handler() {
////        return message -> {
////            try {
////                String payloadStr = (String) message.getPayload();
////                // log.debug("RAW MSG: {}", payloadStr); // Uncomment only for debugging
////
////                // 1. Convert JSON to Java Object
////                S3Payload payload = mapper.readValue(payloadStr, S3Payload.class);
////
////                // 2. Process
////                if (payload != null && payload.getTopic() != null) {
////                    processAndSave(payload);
////                }
////
////            } catch (Exception e) {
////                log.error("❌ MQTT Error: {}", e.getMessage());
////            }
////        };
////    }
////
////    private void processAndSave(S3Payload payload) {
////        String topic = payload.getTopic();
////
////        // 1. Validation: Does this freezer exist?
////        Optional<Freezer> freezerOpt = freezerRepository.findByFreezerId(topic);
////        if (freezerOpt.isEmpty()) {
////            // log.warn("Ignored data for unknown device: {}", topic);
////            return;
////        }
//    // ✅ THE HANDLER
//    @Bean
//    @ServiceActivator(inputChannel = "mqttInputChannel")
//    public MessageHandler handler() {
//        return message -> {
//            try {
//                String payloadStr = (String) message.getPayload();
//
//                // 👇 UNCOMMENT THIS LINE! 👇
//                log.info("📢 RAW MSG RECEIVED: {}", payloadStr);
//
//                S3Payload payload = mapper.readValue(payloadStr, S3Payload.class);
//
//                if (payload != null && payload.getTopic() != null) {
//                    processAndSave(payload);
//                }
//            } catch (Exception e) {
//                log.error("❌ MQTT Error: {}", e.getMessage());
//            }
//        };
//    }
//
//    private void processAndSave(S3Payload payload) {
//        String topic = payload.getTopic();
//
//        Optional<Freezer> freezerOpt = freezerRepository.findByFreezerId(topic);
//        if (freezerOpt.isEmpty()) {
//            // 👇 UNCOMMENT THIS LINE! 👇
//            log.warn("⚠️ UNKNOWN DEVICE: Database does not have ID: '{}'", topic);
//            return;
//        }
//
//        // ... rest of your code ...
//
//        // 2. ✅ DUPLICATE CHECK (Your Important Logic)
//        LocalDateTime incomingTs = parseTimestamp(payload.getTimestamp());
//        Optional<FreezerReading> lastReading = readingRepository.findTopByFreezerIdOrderByTimestampDesc(topic);
//
//        if (lastReading.isPresent()) {
//            LocalDateTime lastTs = lastReading.get().getTimestamp();
//            if (!incomingTs.isAfter(lastTs)) {
//                return; // ⛔ STOP: We already have this data
//            }
//        }
//
//        // 3. Save Data
//        try {
//            FreezerReading reading = new FreezerReading();
//            reading.setFreezerId(topic);
//
//            // Map Fields
//            String prefix = extractPrefix(topic);
//            boolean isDataLogger = "DL".equalsIgnoreCase(prefix);
//
//            if (payload.getTemperature() != null)
//                reading.setTemperature(BigDecimal.valueOf(payload.getTemperature()));
//
//            if (payload.getAmbientTemperature() != null)
//                reading.setAmbientTemperature(BigDecimal.valueOf(payload.getAmbientTemperature()));
//
//            if (payload.getHumidity() != null)
//                reading.setHumidity(BigDecimal.valueOf(payload.getHumidity()));
//
//            reading.setPo(payload.getPo());
//            reading.setFreezerOn("ON".equalsIgnoreCase(payload.getFreezerPower()));
//
//            if (isDataLogger) {
//                reading.setDoorOpen(null);
//            } else {
//                reading.setDoorOpen("OPEN".equalsIgnoreCase(payload.getFreezerDoor()));
//            }
//
//            reading.setTimestamp(incomingTs);
//
//            // 4. Save to Database
//            FreezerReading saved = freezerReadingService.saveReading(reading);
//
//            // 5. Success Log
//            log.info("⚡ Real-time: ID={} Device={} Temp={}",
//                    saved.getId(), saved.getFreezerId(), saved.getTemperature());
//
//        } catch (Exception ex) {
//            log.error("Error saving for {}: {}", topic, ex.getMessage());
//        }
//    }
//
//    private String extractPrefix(String topic) {
//        if (topic == null) return "";
//        Matcher matcher = TOPIC_PREFIX_PATTERN.matcher(topic);
//        return matcher.find() ? matcher.group(1) : "";
//    }
//
//    private LocalDateTime parseTimestamp(String tsStr) {
//        if (tsStr != null && !tsStr.isBlank()) {
//            try {
//                return LocalDateTime.parse(tsStr, TIMESTAMP_FMT);
//            } catch (Exception e) { /* Ignore */ }
//        }
//        return LocalDateTime.now();
//    }
//}