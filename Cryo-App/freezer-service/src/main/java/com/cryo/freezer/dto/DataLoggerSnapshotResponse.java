package com.cryo.freezer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DataLoggerSnapshotResponse {

    private String deviceId;
    private LocalDateTime timestamp;

    // Header info
    private BigDecimal ambientTemperature;
    private BigDecimal batteryPercentage;
    private String power;

    // Channels
    private List<DataLoggerChannelStatusDto> channels;

    // Summary (for pie chart)
    private long totalChannels;
    private long channelsOn;
    private long channelsOff;
    private long channelsInAlert;

    public DataLoggerSnapshotResponse() {}

    public DataLoggerSnapshotResponse(String deviceId,
                                      LocalDateTime timestamp,
                                      BigDecimal ambientTemperature,
                                      BigDecimal batteryPercentage,
                                      String power,
                                      List<DataLoggerChannelStatusDto> channels,
                                      long totalChannels,
                                      long channelsOn,
                                      long channelsOff,
                                      long channelsInAlert) {

        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.ambientTemperature = ambientTemperature;
        this.batteryPercentage = batteryPercentage;
        this.power = power;
        this.channels = channels;
        this.totalChannels = totalChannels;
        this.channelsOn = channelsOn;
        this.channelsOff = channelsOff;
        this.channelsInAlert = channelsInAlert;
    }

    // getters

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getAmbientTemperature() {
        return ambientTemperature;
    }

    public void setAmbientTemperature(BigDecimal ambientTemperature) {
        this.ambientTemperature = ambientTemperature;
    }

    public BigDecimal getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setBatteryPercentage(BigDecimal batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public List<DataLoggerChannelStatusDto> getChannels() {
        return channels;
    }

    public void setChannels(List<DataLoggerChannelStatusDto> channels) {
        this.channels = channels;
    }

    public long getTotalChannels() {
        return totalChannels;
    }

    public void setTotalChannels(long totalChannels) {
        this.totalChannels = totalChannels;
    }

    public long getChannelsOn() {
        return channelsOn;
    }

    public void setChannelsOn(long channelsOn) {
        this.channelsOn = channelsOn;
    }

    public long getChannelsOff() {
        return channelsOff;
    }

    public void setChannelsOff(long channelsOff) {
        this.channelsOff = channelsOff;
    }

    public long getChannelsInAlert() {
        return channelsInAlert;
    }

    public void setChannelsInAlert(long channelsInAlert) {
        this.channelsInAlert = channelsInAlert;
    }
}
