package com.cryo.freezer.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "data_logger_channel_readings")
public class DataLoggerChannelReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic")
    private String topic; // FK reference to DataLoggerDevice.topic

    @Column(name = "channel_number")
    private String channelNumber;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "status")
    private String status;

    @Column(name = "set_temperature", precision = 5, scale = 2)
    private BigDecimal setTemperature;

    @Column(name = "high_temperature", precision = 5, scale = 2)
    private BigDecimal highTemperature;

    @Column(name = "high_alarm")
    private String highAlarm;

    @Column(name = "low_temperature", precision = 5, scale = 2)
    private BigDecimal lowTemperature;

    @Column(name = "low_alarm")
    private String lowAlarm;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(String channelNumber) {
        this.channelNumber = channelNumber;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getSetTemperature() {
        return setTemperature;
    }

    public void setSetTemperature(BigDecimal setTemperature) {
        this.setTemperature = setTemperature;
    }

    public BigDecimal getHighTemperature() {
        return highTemperature;
    }

    public void setHighTemperature(BigDecimal highTemperature) {
        this.highTemperature = highTemperature;
    }

    public String getHighAlarm() {
        return highAlarm;
    }

    public void setHighAlarm(String highAlarm) {
        this.highAlarm = highAlarm;
    }

    public BigDecimal getLowTemperature() {
        return lowTemperature;
    }

    public void setLowTemperature(BigDecimal lowTemperature) {
        this.lowTemperature = lowTemperature;
    }

    public String getLowAlarm() {
        return lowAlarm;
    }

    public void setLowAlarm(String lowAlarm) {
        this.lowAlarm = lowAlarm;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
// Getters & Setters
}
