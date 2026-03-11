package com.cryo.freezer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class        FreezerChartDataPoint {

    private LocalDateTime timestamp;
    private BigDecimal temperature;
    private Boolean freezerOn;
    private Boolean doorOpen;

    public FreezerChartDataPoint(LocalDateTime timestamp,
                                 BigDecimal temperature,
                                 Boolean freezerOn,
                                 Boolean doorOpen) {
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.freezerOn = freezerOn;
        this.doorOpen = doorOpen;
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
}
