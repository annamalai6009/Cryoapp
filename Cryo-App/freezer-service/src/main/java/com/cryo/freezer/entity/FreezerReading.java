package com.cryo.freezer.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "freezer_readings")
public class FreezerReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "freezer_id", nullable = false)
    private String freezerId;
    @Column(name = "po_number")
    private String poNumber;

    @Column(name = "temperature", precision = 5, scale = 2)
    private BigDecimal temperature;
    @Column(name = "ambient_temperature", precision = 5, scale = 2)
    private BigDecimal ambientTemperature;
    @Column(name = "humidity", precision = 5, scale = 2)
    private BigDecimal humidity;

    @Column(name = "freezer_on")
    private Boolean freezerOn;
    @Column(name = "door_open")
    private Boolean doorOpen;
    @Column(name = "door_alarm")
    private Boolean doorAlarm;
    @Column(name = "power_alarm")
    private Boolean powerAlarm;

    @Column(name = "compressor_temp", precision = 5, scale = 2)
    private BigDecimal compressorTemp;
    @Column(name = "freezer_compressor")
    private Boolean freezerCompressor;
    @Column(name = "condenser_temp", precision = 5, scale = 2)
    private BigDecimal condenserTemp;

    @Column(name = "set_temp", precision = 5, scale = 2)
    private BigDecimal setTemp;
    @Column(name = "high_temp", precision = 5, scale = 2)
    private BigDecimal highTemp;
    @Column(name = "high_temp_alarm")
    private Boolean highTempAlarm;
    @Column(name = "low_temp", precision = 5, scale = 2)
    private BigDecimal lowTemp;
    @Column(name = "low_temp_alarm")
    private Boolean lowTempAlarm;

    @Column(name = "battery_percentage", precision = 5, scale = 2)
    private BigDecimal batteryPercentage;
    @Column(name = "battery_alarm")
    private Boolean batteryAlarm;

    @Column(name = "ac_voltage", precision = 6, scale = 2)
    private BigDecimal acVoltage;
    @Column(name = "ac_current", precision = 5, scale = 2)
    private BigDecimal acCurrent;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "red_alert")
    private Boolean redAlert = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    // =============================
    // GETTERS & SETTERS
    // =============================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFreezerId() {
        return freezerId;
    }

    public void setFreezerId(String freezerId) {
        this.freezerId = freezerId;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
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

    public Boolean getBatteryAlarm() {
        return batteryAlarm;
    }

    public void setBatteryAlarm(Boolean batteryAlarm) {
        this.batteryAlarm = batteryAlarm;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getRedAlert() {
        return redAlert;
    }

    public void setRedAlert(Boolean redAlert) {
        this.redAlert = redAlert;
    }

    // ✅ IMPORTANT: Add this method (this fixes your error)
    public Boolean isRedAlert() {
        return redAlert != null && redAlert;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }
}
