package com.cryo.export.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FreezerReadingExportDto {

    private LocalDateTime timestamp;
    private BigDecimal temperature;
    private Boolean freezerOn;
    private Boolean doorOpen;

    @JsonProperty("isRedAlert")
    private Boolean isRedAlert;   // main JSON field

    public FreezerReadingExportDto() {
    }

    public FreezerReadingExportDto(LocalDateTime timestamp,
                                   BigDecimal temperature,
                                   Boolean freezerOn,
                                   Boolean doorOpen,
                                   Boolean isRedAlert) {
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.freezerOn = freezerOn;
        this.doorOpen = doorOpen;
        this.isRedAlert = isRedAlert;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
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

    public Boolean getIsRedAlert() {
        return isRedAlert;
    }

    public void setIsRedAlert(Boolean isRedAlert) {
        this.isRedAlert = isRedAlert;
    }

    // 👇 Extra setter to support JSON field "redAlert"
    @JsonProperty("redAlert")
    public void mapRedAlert(Boolean redAlert) {
        this.isRedAlert = redAlert;
    }

    @Override
    public String toString() {
        return "FreezerReadingExportDto{" +
                "timestamp=" + timestamp +
                ", temperature=" + temperature +
                ", freezerOn=" + freezerOn +
                ", doorOpen=" + doorOpen +
                ", isRedAlert=" + isRedAlert +
                '}';
    }
}
