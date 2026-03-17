package com.cryo.freezer.service;

import com.cryo.freezer.entity.DataLoggerChannelReading;
import com.cryo.freezer.event.DataLoggerAlertEvent;
import com.cryo.freezer.repository.DataLoggerChannelRepository;
import com.cryo.freezer.repository.DataLoggerDeviceRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class DataLoggerService {

    private final DataLoggerChannelRepository channelRepository;
    private final DataLoggerDeviceRepository deviceRepository;
    private final WebClient authServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DataLoggerService(
            DataLoggerChannelRepository channelRepository,
            DataLoggerDeviceRepository deviceRepository, @Value("${auth.service.url:http://localhost:8081}") String authServiceUrl,
            KafkaTemplate<String, Object> kafkaTemplate) {

        this.channelRepository = channelRepository;
        this.deviceRepository = deviceRepository;
        this.authServiceClient = WebClient.builder().baseUrl(authServiceUrl).build();
        this.kafkaTemplate = kafkaTemplate;
    }
    @Transactional
    public void saveChannelReading(DataLoggerChannelReading channel) {

        // Normalize channel number so DB + APIs are consistent (e.g. "CH1" -> "1")
        channel.setChannelNumber(normalizeChannelNumber(channel.getChannelNumber()));

        channelRepository.save(channel);

        // 🔥 PUBLISH KAFKA EVENT
        DataLoggerAlertEvent event =
                new DataLoggerAlertEvent(
                        channel.getTopic(),
                        channel.getChannelNumber(),
                        channel.getTemperature(),
                        "ON".equalsIgnoreCase(channel.getHighAlarm()),   // ✅ FIXED
                        "ON".equalsIgnoreCase(channel.getLowAlarm()),    // ✅ FIXED
                        channel.getTimestamp()
                );

        kafkaTemplate.send("datalogger-alert-topic", event);
    }

    private String normalizeChannelNumber(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return s;

        // Accept CH1 / ch01 / " CH 1 " and normalize to "1"
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("^ch\\s*0*(\\d+)$", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(s.replace(" ", ""));
        if (m.matches()) {
            return m.group(1);
        }

        // Keep pure numeric channels as-is (trimmed)
        if (s.matches("^\\d+$")) {
            return s;
        }

        // Unknown format: store as trimmed string
        return s;
    }

}
