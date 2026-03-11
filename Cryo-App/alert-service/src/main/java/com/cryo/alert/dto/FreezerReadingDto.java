// FreezerReadingDto
package com.cryo.alert.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FreezerReadingDto {
    private String freezerId;
    private BigDecimal temperature;
    private Boolean freezerOn;
    private Boolean doorOpen;
    private LocalDateTime timestamp;

    public FreezerReadingDto() {
    }

    public FreezerReadingDto(String freezerId, BigDecimal temperature,
                             Boolean freezerOn, Boolean doorOpen,
                             LocalDateTime timestamp) {
        this.freezerId = freezerId;
        this.temperature = temperature;
        this.freezerOn = freezerOn;
        this.doorOpen = doorOpen;
        this.timestamp = timestamp;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
