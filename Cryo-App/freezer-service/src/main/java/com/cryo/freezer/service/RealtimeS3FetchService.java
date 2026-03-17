package com.cryo.freezer.service;

import com.cryo.freezer.dto.DataLoggerS3Payload;
import com.cryo.freezer.dto.S3Payload;
import com.cryo.freezer.entity.*;
import com.cryo.freezer.repository.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public class RealtimeS3FetchService {

    private static final Logger log = LoggerFactory.getLogger(RealtimeS3FetchService.class);

    private final FreezerReadingService freezerReadingService;
    private final FreezerReadingRepository freezerReadingRepository;
    private final FreezerRepository freezerRepository;
    private final DataLoggerDeviceRepository dlDeviceRepository;
    private final DataLoggerChannelRepository dlChannelRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final DataLoggerService dataLoggerService;


    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    // 🔥 MEMORY CACHE (NO DB DUPLICATE CHECK)
    private final Map<String, LocalDateTime> lastFreezerMap = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastDataLoggerMap = new ConcurrentHashMap<>();

    private static final DateTimeFormatter DL_TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public RealtimeS3FetchService(
            FreezerReadingService freezerReadingService,
            FreezerReadingRepository freezerReadingRepository,
            FreezerRepository freezerRepository,
            DataLoggerDeviceRepository dlDeviceRepository,
            DataLoggerChannelRepository dlChannelRepository,
            RestTemplate restTemplate, DataLoggerService dataLoggerService) {

        this.freezerReadingService = freezerReadingService;
        this.freezerReadingRepository = freezerReadingRepository;
        this.freezerRepository = freezerRepository;
        this.dlDeviceRepository = dlDeviceRepository;
        this.dlChannelRepository = dlChannelRepository;
        this.restTemplate = restTemplate;
        this.dataLoggerService = dataLoggerService;

        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }

    // =========================================================
    // MAIN POLLING
    // =========================================================

    public void fetchAllActiveFreezers() {

        List<Freezer> activeFreezers =
                freezerRepository.findByStatusAndS3UrlIsNotNull(Freezer.FreezerStatus.ACTIVE);

        log.info("Realtime S3 poll - active freezers found: {}", activeFreezers.size());

        List<CompletableFuture<Void>> futures = activeFreezers.stream()
                .map(f -> CompletableFuture.runAsync(() -> fetchOne(f), executorService))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void fetchOne(Freezer freezer) {

        try {
            log.info("Fetching S3 data for PO {} from URL {}", freezer.getPoNumber(), freezer.getS3Url());
            String json = restTemplate.getForObject(freezer.getS3Url(), String.class);

            if (json == null || json.isBlank()) return;

            // =====================================================
            // 🔥 DEVICE TYPE BASED ROUTING (NO MORE JSON CHECK)
            // =====================================================

            if (freezer.getDeviceType() == Freezer.DeviceType.DATA_LOGGER) {
                processDataLogger(json);
            } else {
                processNormalFreezer(json, freezer);
            }

        } catch (Exception e) {
            log.error("S3 fetch failed for PO {} from URL {}",
                    freezer.getPoNumber(), freezer.getS3Url(), e);
        }
    }


    // =========================================================
    // NORMAL FREEZER (CONTROLLER)
    // =========================================================

    private void processNormalFreezer(String json, Freezer freezer) throws Exception {

        S3Payload payload = mapper.readValue(json, S3Payload.class);

        if (payload.getTopic() == null) return;

        if (freezer.getFreezerId() == null) {
            freezer.setFreezerId(payload.getTopic());
            freezerRepository.save(freezer);
        }
        LocalDateTime timestamp = parseIsoTimestamp(payload.getTimestamp());
        // Optional<FreezerReading> last =
//                freezerReadingRepository.findTopByFreezerIdOrderByTimestampDesc(payload.getTopic());
//
//        if (last.isPresent() && !timestamp.isAfter(last.get().getTimestamp())) {
//            return; // skip duplicate
//        }


        // 🔥 MEMORY DUPLICATE CHECK(uncomment once realtime data is pushed)
        LocalDateTime lastTs = lastFreezerMap.get(payload.getTopic());
        if (lastTs != null && !timestamp.isAfter(lastTs)) {
            return;
        }
        FreezerReading reading = new FreezerReading();
        reading.setFreezerId(payload.getTopic());
        reading.setPoNumber(payload.getPo());
        reading.setTemperature(toBig(payload.getTemperature()));
        reading.setAmbientTemperature(toBig(payload.getAmbientTemperature()));
        reading.setHumidity(toBig(payload.getHumidity()));
        reading.setFreezerOn("ON".equalsIgnoreCase(payload.getFreezerPower()));
        reading.setDoorOpen("OPEN".equalsIgnoreCase(payload.getFreezerDoor()));
        reading.setDoorAlarm("ON".equalsIgnoreCase(payload.getDoorAlarm()));
        reading.setPowerAlarm("ON".equalsIgnoreCase(payload.getPowerAlarm()));
        reading.setCompressorTemp(toBig(payload.getCompressorTemp()));
        reading.setFreezerCompressor("ON".equalsIgnoreCase(payload.getFreezerCompressor()));
        reading.setCondenserTemp(toBig(payload.getCondenserTemp()));
        reading.setSetTemp(toBig(payload.getSetTemp()));
        reading.setHighTemp(toBig(payload.getHighTemp()));
        reading.setLowTemp(toBig(payload.getLowTemp()));
        reading.setHighTempAlarm("ON".equalsIgnoreCase(payload.getHighTempAlarm()));
        reading.setLowTempAlarm("ON".equalsIgnoreCase(payload.getLowTempAlarm()));
        reading.setBatteryPercentage(toBig(payload.getBatteryPercentage()));
        reading.setBatteryAlarm("ON".equalsIgnoreCase(payload.getBatteryPercentAlarm()));
        reading.setAcVoltage(toBig(payload.getAcVolatage()));
        reading.setAcCurrent(toBig(payload.getAcCurrent()));

        reading.setTimestamp(timestamp);

        // 🔥 NEW RED ALERT LOGIC USING PAYLOAD VALUES
        if (reading.getTemperature() != null &&
                reading.getHighTemp() != null &&
                reading.getLowTemp() != null) {

            boolean red =
                    reading.getTemperature().compareTo(reading.getLowTemp()) < 0 ||
                            reading.getTemperature().compareTo(reading.getHighTemp()) > 0;

            reading.setRedAlert(red);
        } else {
            reading.setRedAlert(false);
        }

        freezerReadingService.saveReading(reading);
        // 🔥 UPDATE MEMORY
        lastFreezerMap.put(payload.getTopic(), timestamp);
        log.info(
                "❄ FREEZER DATA | ID: {} | Temp: {}°C | Time: {}",
                reading.getFreezerId(),
                reading.getTemperature(),
                reading.getTimestamp()
        );

    }


    // =========================================================
    // DATA LOGGER (COMMON + CHANNELS)
    // =========================================================

    private void processDataLogger(String json) throws Exception {

        DataLoggerS3Payload payload =
                mapper.readValue(json, DataLoggerS3Payload.class);

        DataLoggerS3Payload.Common c = payload.getCommon();

        if (c == null || c.topic == null) return;

        LocalDateTime ts = LocalDateTime.parse(c.timestamp, DL_TIMESTAMP_FMT);
        // 🔥 MEMORY DUPLICATE CHECK(uncomment once realtime data is pushed)
        LocalDateTime lastTs = lastDataLoggerMap.get(c.topic);
        if (lastTs != null && !ts.isAfter(lastTs)) {
            return;
        }

        // Save / Update common device
        DataLoggerDevice device = dlDeviceRepository
                .findById(c.topic)
                .orElse(new DataLoggerDevice());

        device.setTopic(c.topic);
        device.setPoNumber(c.po);
        device.setPower(c.power);
        device.setPowerAlarm(c.powerAlarm);
        device.setBatteryPercentage(toBig(c.batteryPercentage));
        device.setBatteryAlarm(c.batteryAlarm);
        device.setAmbientTemperature(toBig(c.ambientTemperature));
        device.setAmbientHumidity(toBig(c.ambientHumidity));
        device.setSetTemperature(toBig(c.setTemperature));
        device.setLastTimestamp(ts);

        dlDeviceRepository.save(device);

        // ✅ Link the registered "Freezer" row (by PO) to this data logger topic for dashboard + routing.
        // Without this, Freezer.freezerId stays null and UI shows Topic/ID as "—" and View can't open.
        if (c.po != null && !c.po.isBlank()) {
            freezerRepository.findByPoNumber(c.po)
                    .ifPresent(f -> {
                        if (f.getFreezerId() == null || f.getFreezerId().isBlank()) {
                            f.setFreezerId(c.topic);
                            freezerRepository.save(f);
                            log.info("Linked DATA LOGGER PO {} -> topic {}", c.po, c.topic);
                        }
                    });
        }

        // 🔥 DYNAMIC CHANNELS
        int totalChannels = 0;
        if (payload.getChannels() != null) {

            totalChannels = payload.getChannels().size();
            for (DataLoggerS3Payload.Channel ch : payload.getChannels()) {
                DataLoggerChannelReading channel = new DataLoggerChannelReading();

                channel.setTopic(c.topic);
                channel.setChannelNumber(ch.channelNumber);
                channel.setTemperature(toBig(ch.temperature));
                channel.setStatus(ch.status);
                channel.setSetTemperature(toBig(ch.setTemperature));
                channel.setHighTemperature(toBig(ch.highTemperature));
                channel.setLowTemperature(toBig(ch.lowTemperature));
                channel.setHighAlarm(ch.highAlarm);
                channel.setLowAlarm(ch.lowAlarm);
                channel.setTimestamp(ts);

                //dlChannelRepository.save(channel);
                dataLoggerService.saveChannelReading(channel);


//                log.info(
//                        "📊 DATA LOGGER | ID: {} | Channels Received: {} | Time: {}",
//                        c.topic,
//                        totalChannels,
//                        ts
//                );


            }
        }
        // 🔥 UPDATE MEMORY
        lastDataLoggerMap.put(c.topic, ts);

        log.info(
                "📊 DATA LOGGER | ID: {} | Channels Received: {} | Time: {}",
                c.topic,
                totalChannels,
                ts
        );
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private BigDecimal toBig(Object val) {
        if (val == null) return null;
        return new BigDecimal(val.toString());
    }

    private LocalDateTime parseIsoTimestamp(String ts) {
        try {
            return OffsetDateTime.parse(ts).toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
