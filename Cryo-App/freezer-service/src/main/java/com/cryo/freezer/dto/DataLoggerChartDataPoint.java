package com.cryo.freezer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DataLoggerChartDataPoint {

    private LocalDateTime timestamp;
    private BigDecimal temperature;
    private String channelNumber;

    public DataLoggerChartDataPoint(LocalDateTime timestamp,
                                    BigDecimal temperature,
                                    String channelNumber) {
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.channelNumber = channelNumber;
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

    public String getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(String channelNumber) {
        this.channelNumber = channelNumber;
    }
}
