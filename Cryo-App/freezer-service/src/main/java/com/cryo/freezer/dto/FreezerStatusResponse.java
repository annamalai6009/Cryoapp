package com.cryo.freezer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

/** Include all keys in JSON so frontend always receives full structure (null when not set). */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class FreezerStatusResponse {

    // ===== NORMAL FREEZER FIELDS =====
    private BigDecimal temperature;
    private Boolean freezerOn;
    private Boolean doorOpen;
    private Boolean isRedAlert;

    // Extended telemetry for NORMAL FREEZER (matches JSON example)
    private BigDecimal ambientTemperature;   // ambientTemperature
    private BigDecimal humidity;            // humidity

    private Boolean doorAlarm;              // doorAlarm
    private Boolean powerAlarm;             // powerAlarm

    private BigDecimal compressorTemp;      // compressorTemp
    private Boolean freezerCompressor;      // freezerCompressor
    private BigDecimal condenserTemp;       // condenserTemp

    private BigDecimal setTemp;             // setTemp
    private BigDecimal highTemp;            // highTemp
    private Boolean highTempAlarm;          // highTempAlarm
    private BigDecimal lowTemp;             // lowTemp
    private Boolean lowTempAlarm;           // lowTempAlarm

    private BigDecimal batteryPercentage;   // batteryPercentage
    private Boolean batteryPercentAlarm;    // batteryPercentAlarm

    private BigDecimal acVoltage;           // acVoltage
    private BigDecimal acCurrent;           // acCurrent

    // ===== DATA LOGGER COMMON FIELDS =====
    private String power;                   // data logger power string
    private List<DataLoggerChannelStatusDto> channels;

    private LocalDateTime timestamp;

    public FreezerStatusResponse() {}

    // Constructor for NORMAL FREEZER
    public FreezerStatusResponse(BigDecimal temperature,
                                 Boolean freezerOn,
                                 Boolean doorOpen,
                                 Boolean isRedAlert,
                                 LocalDateTime timestamp) {
        this.temperature = temperature;
        this.freezerOn = freezerOn;
        this.doorOpen = doorOpen;
        this.isRedAlert = isRedAlert;
        this.timestamp = timestamp;
    }

    // Constructor for DATA LOGGER
    public FreezerStatusResponse(BigDecimal ambientTemperature,
                                 BigDecimal batteryPercentage,
                                 String power,
                                 List<DataLoggerChannelStatusDto> channels,
                                 LocalDateTime timestamp) {
        this.ambientTemperature = ambientTemperature;
        this.batteryPercentage = batteryPercentage;
        this.power = power;
        this.channels = channels;
        this.timestamp = timestamp;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public Boolean getFreezerOn() {
        return freezerOn;
    }

    public void setFreezerOn(Boolean freezerOn) {
        this.freezerOn = freezerOn;
    }

    public Boolean getDoorOpen() {
        return doorOpen;
    }

    public void setDoorOpen(Boolean doorOpen) {
        this.doorOpen = doorOpen;
    }

    public Boolean getRedAlert() {
        return isRedAlert;
    }

    public void setRedAlert(Boolean redAlert) {
        isRedAlert = redAlert;
    }

    public BigDecimal getAmbientTemperature() {
        return ambientTemperature;
    }

    public void setAmbientTemperature(BigDecimal ambientTemperature) {
        this.ambientTemperature = ambientTemperature;
    }

    public BigDecimal getHumidity() {
        return humidity;
    }

    public void setHumidity(BigDecimal humidity) {
        this.humidity = humidity;
    }

    public Boolean getDoorAlarm() {
        return doorAlarm;
    }

    public void setDoorAlarm(Boolean doorAlarm) {
        this.doorAlarm = doorAlarm;
    }

    public Boolean getPowerAlarm() {
        return powerAlarm;
    }

    public void setPowerAlarm(Boolean powerAlarm) {
        this.powerAlarm = powerAlarm;
    }

    public BigDecimal getCompressorTemp() {
        return compressorTemp;
    }

    public void setCompressorTemp(BigDecimal compressorTemp) {
        this.compressorTemp = compressorTemp;
    }

    public Boolean getFreezerCompressor() {
        return freezerCompressor;
    }

    public void setFreezerCompressor(Boolean freezerCompressor) {
        this.freezerCompressor = freezerCompressor;
    }

    public BigDecimal getCondenserTemp() {
        return condenserTemp;
    }

    public void setCondenserTemp(BigDecimal condenserTemp) {
        this.condenserTemp = condenserTemp;
    }

    public BigDecimal getSetTemp() {
        return setTemp;
    }

    public void setSetTemp(BigDecimal setTemp) {
        this.setTemp = setTemp;
    }

    public BigDecimal getHighTemp() {
        return highTemp;
    }

    public void setHighTemp(BigDecimal highTemp) {
        this.highTemp = highTemp;
    }

    public Boolean getHighTempAlarm() {
        return highTempAlarm;
    }

    public void setHighTempAlarm(Boolean highTempAlarm) {
        this.highTempAlarm = highTempAlarm;
    }

    public BigDecimal getLowTemp() {
        return lowTemp;
    }

    public void setLowTemp(BigDecimal lowTemp) {
        this.lowTemp = lowTemp;
    }

    public Boolean getLowTempAlarm() {
        return lowTempAlarm;
    }

    public void setLowTempAlarm(Boolean lowTempAlarm) {
        this.lowTempAlarm = lowTempAlarm;
    }

    public BigDecimal getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setBatteryPercentage(BigDecimal batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    public Boolean getBatteryPercentAlarm() {
        return batteryPercentAlarm;
    }

    public void setBatteryPercentAlarm(Boolean batteryPercentAlarm) {
        this.batteryPercentAlarm = batteryPercentAlarm;
    }

    public BigDecimal getAcVoltage() {
        return acVoltage;
    }

    public void setAcVoltage(BigDecimal acVoltage) {
        this.acVoltage = acVoltage;
    }

    public BigDecimal getAcCurrent() {
        return acCurrent;
    }

    public void setAcCurrent(BigDecimal acCurrent) {
        this.acCurrent = acCurrent;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
