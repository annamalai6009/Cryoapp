package com.cryo.freezer.service;

import com.cryo.common.exception.BadRequestException;
import com.cryo.common.exception.ResourceNotFoundException;
import com.cryo.freezer.dto.*;
import com.cryo.freezer.entity.*;
import com.cryo.freezer.repository.*;
import com.cryo.freezer.util.UserContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FreezerService {
    private static final Logger logger = LoggerFactory.getLogger(FreezerService.class);

    private final FreezerRepository freezerRepository;
    private final FreezerReadingRepository freezerReadingRepository;
    private final DeviceInventoryRepository deviceInventoryRepository;
    private final FreezerReadingService readingService; // ✅ Inject ReadingService
    private final DataLoggerDeviceRepository dlDeviceRepository;
    private final DataLoggerChannelRepository dataLoggerChannelRepository;


    public FreezerService(FreezerRepository freezerRepository,
                          FreezerReadingRepository freezerReadingRepository,
                          DeviceInventoryRepository deviceInventoryRepository,
                          FreezerReadingService readingService, DataLoggerDeviceRepository dlDeviceRepository, DataLoggerChannelRepository dataLoggerChannelRepository) { // ✅ Add to Constructor
        this.freezerRepository = freezerRepository;
        this.freezerReadingRepository = freezerReadingRepository;
        this.deviceInventoryRepository = deviceInventoryRepository;
        this.readingService = readingService;
        this.dlDeviceRepository = dlDeviceRepository;
        this.dataLoggerChannelRepository = dataLoggerChannelRepository;
    }

    @Transactional
    public Freezer registerNewFreezer(String ownerUserId, FreezerRegisterRequest request) {
        DeviceInventory inventoryItem = deviceInventoryRepository.findByPoNumber(request.getPoNumber())
                .orElseThrow(() -> new BadRequestException("Invalid PO Number. Please contact support."));

        if (freezerRepository.existsByPoNumber(request.getPoNumber())) {
            throw new BadRequestException("PO Number is already active.");
        }

        Freezer newFreezer = new Freezer();
        newFreezer.setPoNumber(request.getPoNumber());
        newFreezer.setOwnerUserId(ownerUserId);
        newFreezer.setName(request.getName());
        newFreezer.setStatus(Freezer.FreezerStatus.ACTIVE);
        newFreezer.setS3Url(inventoryItem.getS3Url());
        // ✅ CORRECT WAY — get type from inventory
        newFreezer.setDeviceType(inventoryItem.getDeviceType());


        Freezer savedFreezer = freezerRepository.save(newFreezer);

        inventoryItem.setIsClaimed(true);
        deviceInventoryRepository.save(inventoryItem);

        logger.info("Freezer registered: PO={} Owner={}", request.getPoNumber(), ownerUserId);
        return savedFreezer;
    }


//    @Transactional
//    public void updateFreezerSettings(String freezerId, String ownerUserId, FreezerSettingsRequest request) {
//        Freezer freezer = freezerRepository.findByFreezerIdAndOwnerUserId(freezerId, ownerUserId)
//                .orElseThrow(() -> new ResourceNotFoundException("Freezer", freezerId));
//
//        if (request.getMinThreshold().compareTo(request.getMaxThreshold()) >= 0) {
//            throw new BadRequestException("Min threshold must be less than Max threshold.");
//        }
//        freezerRepository.save(freezer);
//    }


    public List<FreezerResponse> getAllFreezersForUser(String ownerUserId) {

        List<Freezer> devices = freezerRepository.findByOwnerUserId(ownerUserId);

        return devices.stream()
                .map(device -> new FreezerResponse(
                        device.getId(),
                        device.getFreezerId(),
                        device.getName(),
                        device.getStatus().name(),
                        device.getDeviceType().name()   // ✅ CRITICAL
                ))
                .collect(Collectors.toList());
    }


    public FreezerSummaryResponse getFreezerSummary(String ownerUserId) {

        List<Freezer> devices = freezerRepository.findByOwnerUserId(ownerUserId);
        if (devices.isEmpty()) {
            return new FreezerSummaryResponse(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        long totalFreezers = 0;
        long activeFreezersCount = 0;
        long freezersOnCount = 0;
        long freezersOffCount = 0;
        long redAlertFreezersCount = 0;

        long totalDataLoggers = 0;
        long totalChannels = 0;
        long channelsSending = 0;
        long channelsNotSending = 0;
        long channelsInAlert = 0;

        long activeDevicesCount = 0;

        for (Freezer device : devices) {
            Freezer.DeviceType deviceType = device.getDeviceType();
            if (deviceType == null) continue; // skip malformed rows

            if (deviceType == Freezer.DeviceType.NORMAL_FREEZER) {

                totalFreezers++;

                if (device.getStatus() == Freezer.FreezerStatus.ACTIVE) {
                    activeFreezersCount++;

                    String fid = device.getFreezerId();
                    FreezerReading latest = (fid != null && !fid.isBlank())
                            ? freezerReadingRepository.findFirstByFreezerIdOrderByTimestampDesc(fid).orElse(null)
                            : null;

                    if (latest != null) {

                        if (Boolean.TRUE.equals(latest.getFreezerOn())) {
                            freezersOnCount++;
                            // Only count as actively monitored when the freezer is actually ON
                            activeDevicesCount++;
                        } else {
                            freezersOffCount++;
                        }

                        if (latest.isRedAlert()) {
                            redAlertFreezersCount++;
                        }
                    }
                }
            }

            // ===============================
            // DATA LOGGER
            // ===============================
            else {

                totalDataLoggers++;

                // Avoid passing null topic to repository (causes 500)
                String dlTopic = device.getFreezerId();
                List<DataLoggerChannelReading> latestChannels = (dlTopic != null && !dlTopic.isBlank())
                        ? dataLoggerChannelRepository.findLatestChannelsByTopic(dlTopic)
                        : List.of();

                totalChannels += latestChannels.size();

                long sendingForThisLogger = 0;

                for (DataLoggerChannelReading ch : latestChannels) {

                    // SENDING LOGIC
                    if ("SENDING".equalsIgnoreCase(ch.getStatus())
                            || "ON".equalsIgnoreCase(ch.getStatus())
                            || Boolean.TRUE.equals(ch.getStatus())) {

                        channelsSending++;
                        sendingForThisLogger++;

                    } else {
                        channelsNotSending++;
                    }

                    // ALERT LOGIC
                    if (Boolean.TRUE.equals(ch.getHighAlarm())
                            || Boolean.TRUE.equals(ch.getLowAlarm())) {

                        channelsInAlert++;
                    }
                }

                // Count data logger as actively monitored only if it has at least one sending channel
                if (device.getStatus() == Freezer.FreezerStatus.ACTIVE && sendingForThisLogger > 0) {
                    activeDevicesCount++;
                }
            }
        }

        return new FreezerSummaryResponse(
                totalFreezers,
                activeFreezersCount,
                freezersOnCount,
                freezersOffCount,
                redAlertFreezersCount,
                totalDataLoggers,
                totalChannels,
                channelsSending,
                channelsNotSending,
                channelsInAlert,
                activeDevicesCount
        );
    }


    public FreezerStatusResponse getFreezerStatus(String freezerId) {

        String ownerUserId = UserContext.getUserId();

        Freezer freezer = (ownerUserId != null)
                ? freezerRepository.findByFreezerIdAndOwnerUserId(freezerId, ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Freezer", freezerId))
                : freezerRepository.findByFreezerId(freezerId)
                .orElseThrow(() -> new ResourceNotFoundException("Freezer", freezerId));

        // ===============================
        // 🔥 NORMAL FREEZER
        // ===============================
        if (freezer.getDeviceType() == Freezer.DeviceType.NORMAL_FREEZER) {

            FreezerReading latestReading =
                    freezerReadingRepository
                            .findFirstByFreezerIdOrderByTimestampDesc(freezer.getFreezerId())
                            .orElseThrow(() -> new ResourceNotFoundException("Freezer reading", freezerId));

            FreezerStatusResponse response = new FreezerStatusResponse(
                    latestReading.getTemperature(),
                    latestReading.getFreezerOn(),
                    latestReading.getDoorOpen(),
                    latestReading.isRedAlert(),
                    latestReading.getTimestamp()
            );

            // Map extended telemetry fields (controller JSON)
            response.setAmbientTemperature(latestReading.getAmbientTemperature());
            response.setHumidity(latestReading.getHumidity());

            response.setDoorAlarm(latestReading.getDoorAlarm());
            response.setPowerAlarm(latestReading.getPowerAlarm());

            response.setCompressorTemp(latestReading.getCompressorTemp());
            response.setFreezerCompressor(latestReading.getFreezerCompressor());
            response.setCondenserTemp(latestReading.getCondenserTemp());

            response.setSetTemp(latestReading.getSetTemp());
            response.setHighTemp(latestReading.getHighTemp());
            response.setHighTempAlarm(latestReading.getHighTempAlarm());
            response.setLowTemp(latestReading.getLowTemp());
            response.setLowTempAlarm(latestReading.getLowTempAlarm());

            response.setBatteryPercentage(latestReading.getBatteryPercentage());
            response.setBatteryPercentAlarm(latestReading.getBatteryAlarm());

            response.setAcVoltage(latestReading.getAcVoltage());
            response.setAcCurrent(latestReading.getAcCurrent());

            return response;

        }

        // =====================================================
        // 🔥 DATA LOGGER
        // =====================================================
        else {

            DataLoggerDevice device =
                    dlDeviceRepository.findById(freezer.getFreezerId())
                            .orElseThrow(() -> new ResourceNotFoundException("DataLogger", freezerId));

            List<DataLoggerChannelReading> latestChannels =
                    dataLoggerChannelRepository.findLatestChannelsByTopic(freezer.getFreezerId());

            List<DataLoggerChannelStatusDto> channelDtos =
                    latestChannels.stream()
                            .map(ch -> new DataLoggerChannelStatusDto(
                                    normalizeChannelNumber(ch.getChannelNumber()),
                                    ch.getTemperature(),
                                    ch.getStatus(),
                                    ch.getSetTemperature(),
                                    ch.getHighTemperature(),
                                    ch.getLowTemperature(),
                                    "ON".equalsIgnoreCase(ch.getHighAlarm()),   // ✅ FIXED
                                    "ON".equalsIgnoreCase(ch.getLowAlarm()),    // ✅ FIXED
                                    ch.getTimestamp()
                            ))
                            .toList();

            FreezerStatusResponse response = new FreezerStatusResponse(
                    device.getAmbientTemperature(),
                    device.getBatteryPercentage(),
                    device.getPower(),
                    channelDtos,
                    device.getLastTimestamp()
            );
            // Populate exact datalogger JSON keys (no anonymous subclass — avoids 500)
            response.setPowerAlarm(device.getPowerAlarm() != null && "ON".equalsIgnoreCase(device.getPowerAlarm().trim()));
            if (device.getBatteryAlarm() != null) {
                response.setBatteryAlarm(device.getBatteryAlarm());
            }
            if (device.getAmbientHumidity() != null) {
                response.setAmbientHumidity(device.getAmbientHumidity());
            }
            return response;
        }


    }


    public List<?> getFreezerChartData(
            String freezerId,
            LocalDateTime from,
            LocalDateTime to,
            String channel) {

        String ownerUserId = UserContext.getUserId();

        Freezer freezer = (ownerUserId != null)
                ? freezerRepository.findByFreezerIdAndOwnerUserId(freezerId, ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Freezer", freezerId))
                : freezerRepository.findByFreezerId(freezerId)
                .orElseThrow(() -> new ResourceNotFoundException("Freezer", freezerId));

        // ================= NORMAL FREEZER =================
        if (freezer.getDeviceType() == Freezer.DeviceType.NORMAL_FREEZER) {

            List<FreezerReading> readings =
                    freezerReadingRepository
                            .findByFreezerIdAndTimestampBetweenOrderByTimestampAsc(
                                    freezer.getFreezerId(), from, to);

            return readings.stream()
                    .map(r -> new FreezerChartDataPoint(
                            r.getTimestamp(),
                            r.getTemperature(),
                            r.getFreezerOn(),
                            r.getDoorOpen()
                    ))
                    .toList();
        }

        // ================= DATA LOGGER =================
        else {

            // If no channel is provided from the client, default to channel "1"
            if (channel == null || channel.isBlank()) {
                channel = "1";
            }
            String normalizedChannel = normalizeChannelNumber(channel);

            // Backward-compatible: older rows may be stored as "CH1" while newer are "1"
            java.util.List<String> channelCandidates = new java.util.ArrayList<>();
            if (normalizedChannel != null && !normalizedChannel.isBlank()) {
                channelCandidates.add(normalizedChannel);
                channelCandidates.add("CH" + normalizedChannel);
                channelCandidates.add("ch" + normalizedChannel);
            } else {
                channelCandidates.add(channel.trim());
            }
            channelCandidates = channelCandidates.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .distinct()
                    .toList();

            List<DataLoggerChannelReading> readings =
                    dataLoggerChannelRepository
                            .findByTopicAndChannelNumberInAndTimestampBetweenOrderByTimestampAsc(
                                    freezer.getFreezerId(),
                                    channelCandidates,
                                    from,
                                    to);

            return readings.stream()
                    .map(r -> new DataLoggerChartDataPoint(
                            r.getTimestamp(),
                            r.getTemperature(),
                            normalizeChannelNumber(r.getChannelNumber())
                    ))
                    .toList();
        }
    }

    private String normalizeChannelNumber(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return s;

        // Accept CH1 / ch01 / "CH 1" and normalize to "1"
        String compact = s.replace(" ", "");
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("^ch0*(\\d+)$", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(compact);
        if (m.matches()) {
            return m.group(1);
        }

        if (s.matches("^\\d+$")) {
            return s;
        }

        return s;
    }


    public DataLoggerSnapshotResponse getLatestChannelSnapshot(String freezerId) {

        Freezer freezer = freezerRepository.findByFreezerId(freezerId)
                .orElseThrow(() -> new ResourceNotFoundException("Freezer", freezerId));

        if (freezer.getDeviceType() != Freezer.DeviceType.DATA_LOGGER) {
            throw new BadRequestException("Not a data logger device");
        }

        DataLoggerDevice device = dlDeviceRepository.findById(freezerId)
                .orElseThrow(() -> new ResourceNotFoundException("DataLogger", freezerId));

        List<DataLoggerChannelReading> latestChannels =
                dataLoggerChannelRepository.findLatestChannelsByTopic(freezerId);

        List<DataLoggerChannelStatusDto> channelDtos =
                latestChannels.stream()
                        .map(ch -> new DataLoggerChannelStatusDto(
                                normalizeChannelNumber(ch.getChannelNumber()),
                                ch.getTemperature(),
                                ch.getStatus(),
                                ch.getSetTemperature(),
                                ch.getHighTemperature(),
                                ch.getLowTemperature(),
                                "ON".equalsIgnoreCase(ch.getHighAlarm()),
                                "ON".equalsIgnoreCase(ch.getLowAlarm()),
                                ch.getTimestamp()
                        ))
                        .toList();

        long total = channelDtos.size();

        long channelsOn = channelDtos.stream()
                .filter(c -> "ON".equalsIgnoreCase(c.getStatus()))
                .count();

        long channelsOff = total - channelsOn;

        long channelsInAlert = channelDtos.stream()
                .filter(c -> Boolean.TRUE.equals(c.getHighAlarm())
                        || Boolean.TRUE.equals(c.getLowAlarm()))
                .count();

        return new DataLoggerSnapshotResponse(
                freezerId,
                device.getLastTimestamp(),
                device.getAmbientTemperature(),
                device.getBatteryPercentage(),
                device.getPower(),
                channelDtos,
                total,
                channelsOn,
                channelsOff,
                channelsInAlert
        );
    }


    // ✅ UPDATED: Get Detailed List with AVERAGE Calculation
    public List<FreezerDetailResponse> getFreezerDetailsForUser(String ownerUserId) {
        List<Freezer> freezers = freezerRepository.findByOwnerUserId(ownerUserId);
        if (freezers.isEmpty()) return List.of();

        List<String> activeIds = freezers.stream()
                .filter(f -> Freezer.FreezerStatus.ACTIVE.equals(f.getStatus()) && f.getFreezerId() != null)
                .map(Freezer::getFreezerId)
                .toList();

        List<FreezerReading> readings = List.of();
        if (!activeIds.isEmpty()) {
            readings = freezerReadingRepository.findLatestReadingsForFreezers(activeIds);
        }

        java.util.Map<String, FreezerReading> readingMap = readings.stream()
                .collect(java.util.stream.Collectors.toMap(FreezerReading::getFreezerId, r -> r, (e, r) -> e));

        return freezers.stream().map(f -> {
            Freezer.DeviceType deviceType = f.getDeviceType();
            // ===============================
            // NORMAL FREEZER (Controller)
            // ===============================
            if (deviceType == Freezer.DeviceType.NORMAL_FREEZER) {

                String fid = f.getFreezerId();
                FreezerReading r = fid != null ? readingMap.get(fid) : null;

                // ✅ GET FAST AVERAGE FROM RAM (Does not hit DB); skip if no freezerId
                Double avgTemp = (fid != null && !fid.isBlank())
                        ? readingService.getFastOneMinuteAverage(fid)
                        : null;

                // Fallback: If average is not ready (app just started), use current temp
                if (avgTemp == null && r != null && r.getTemperature() != null) {
                    avgTemp = r.getTemperature().doubleValue();
                }

                return new FreezerDetailResponse(
                        f.getFreezerId(),
                        f.getName(),
                        f.getPoNumber(),
                        f.getStatus() != null ? f.getStatus().name() : Freezer.FreezerStatus.ACTIVE.name(),
                        r != null ? r.getTemperature() : null,
                        r != null ? r.getFreezerOn() : null,
                        r != null ? r.getDoorOpen() : null,
                        r != null ? r.getTimestamp() : null,
                        r != null ? r.isRedAlert() : false,
                        avgTemp // ✅ PASS AVERAGE TO DTO
                );
            }

            // ===============================
            // DATA LOGGER (or unknown type: treat as data logger for safe response)
            // ===============================
            if (deviceType != null && deviceType != Freezer.DeviceType.DATA_LOGGER) {
                // Unknown device type: return minimal row so we don't break the list
                return new FreezerDetailResponse(
                        f.getFreezerId(), f.getName(), f.getPoNumber(),
                        f.getStatus() != null ? f.getStatus().name() : "ACTIVE",
                        null, null, null, null, false, null);
            }
            String topic = f.getFreezerId();
            DataLoggerDevice dl = null;

            // If the registered row doesn't have topic yet, derive it from data_logger_devices using PO (read-only here to avoid 500).
            if ((topic == null || topic.isBlank()) && f.getPoNumber() != null && !f.getPoNumber().isBlank()) {
                DataLoggerDevice byPo = dlDeviceRepository.findByPoNumber(f.getPoNumber()).orElse(null);
                if (byPo != null && byPo.getTopic() != null && !byPo.getTopic().isBlank()) {
                    topic = byPo.getTopic();
                    dl = byPo;
                }
            } else if (topic != null && !topic.isBlank()) {
                dl = dlDeviceRepository.findById(topic).orElse(null);
            }

            // Use CH1 temperature if present; else first channel; else null.
            java.math.BigDecimal dlTemp = null;
            if (topic != null && !topic.isBlank()) {
                List<DataLoggerChannelReading> latestChannels = dataLoggerChannelRepository.findLatestChannelsByTopic(topic);
                if (latestChannels != null && !latestChannels.isEmpty()) {
                    DataLoggerChannelReading ch1 = latestChannels.stream()
                            .filter(ch -> ch.getChannelNumber() != null &&
                                    "1".equalsIgnoreCase(normalizeChannelNumber(ch.getChannelNumber())))
                            .findFirst()
                            .orElse(latestChannels.get(0));
                    dlTemp = ch1.getTemperature();
                }
            }

            return new FreezerDetailResponse(
                    topic,
                    f.getName(),
                    f.getPoNumber(),
                    f.getStatus() != null ? f.getStatus().name() : Freezer.FreezerStatus.ACTIVE.name(),
                    dlTemp,
                    null,
                    null,
                    dl != null ? dl.getLastTimestamp() : null,
                    false,
                    null
            );
        }).collect(Collectors.toList());
    }

    public FreezerConfigResponse getFreezerConfig(String freezerId) {

        Freezer freezer = freezerRepository.findByFreezerId(freezerId).orElseThrow(() -> new ResourceNotFoundException("Freezer", freezerId));
        return new FreezerConfigResponse(freezer.getFreezerId(), freezer.getName());
    }
}
