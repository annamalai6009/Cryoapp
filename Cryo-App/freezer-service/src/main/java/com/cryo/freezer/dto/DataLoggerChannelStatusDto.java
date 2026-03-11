package com.cryo.freezer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DataLoggerChannelStatusDto {

    private String channelNumber;
    private BigDecimal temperature;
    private String status;
    private Boolean highAlarm;
    private Boolean lowAlarm;
    private LocalDateTime timestamp;   // 👈 ADD THIS


    public DataLoggerChannelStatusDto() {}

    public DataLoggerChannelStatusDto(String channelNumber,
                                      BigDecimal temperature,
                                      String status,
                                      Boolean highAlarm,
                                      Boolean lowAlarm, LocalDateTime timestamp) {
        this.channelNumber = channelNumber;
        this.temperature = temperature;
        this.status = status;
        this.highAlarm = highAlarm;
        this.lowAlarm = lowAlarm;
        this.timestamp = timestamp;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getChannelNumber() { return channelNumber; }
    public void setChannelNumber(String channelNumber) { this.channelNumber = channelNumber; }

    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getHighAlarm() { return highAlarm; }
    public void setHighAlarm(Boolean highAlarm) { this.highAlarm = highAlarm; }

    public Boolean getLowAlarm() { return lowAlarm; }
    public void setLowAlarm(Boolean lowAlarm) { this.lowAlarm = lowAlarm; }

}
