package com.cryo.freezer.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DataLoggerAlertEvent {

    private String deviceId;
    private String channelNumber;
    private BigDecimal temperature;
    private Boolean highAlarm;
    private Boolean lowAlarm;
    private LocalDateTime timestamp;

    public DataLoggerAlertEvent() {}

    public DataLoggerAlertEvent(String deviceId,
                                String channelNumber,
                                BigDecimal temperature,
                                Boolean highAlarm,
                                Boolean lowAlarm,
                                LocalDateTime timestamp) {
        this.deviceId = deviceId;
        this.channelNumber = channelNumber;
        this.temperature = temperature;
        this.highAlarm = highAlarm;
        this.lowAlarm = lowAlarm;
        this.timestamp = timestamp;
    }

    // Getters & Setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getChannelNumber() { return channelNumber; }
    public void setChannelNumber(String channelNumber) { this.channelNumber = channelNumber; }

    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }

    public Boolean getHighAlarm() { return highAlarm; }
    public void setHighAlarm(Boolean highAlarm) { this.highAlarm = highAlarm; }

    public Boolean getLowAlarm() { return lowAlarm; }
    public void setLowAlarm(Boolean lowAlarm) { this.lowAlarm = lowAlarm; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
