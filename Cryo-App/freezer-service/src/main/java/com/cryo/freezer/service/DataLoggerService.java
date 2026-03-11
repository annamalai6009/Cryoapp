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


}
