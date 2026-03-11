package com.cryo.freezer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataLoggerS3Payload {

    private Common common;
    private List<Channel> channels;

    // =========================
    // GETTERS & SETTERS
    // =========================

    public Common getCommon() {
        return common;
    }

    public void setCommon(Common common) {
        this.common = common;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    // ====================================================
    // INNER CLASS → COMMON BLOCK
    // ====================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Common {

        public String topic;
        public String po;
        public String timestamp;

        public String power;
        public String powerAlarm;

        public String batteryPercentage;
        public String batteryAlarm;

        public String ambientTemperature;
        public String ambientHumidity;

        public String setTemperature;
    }

    // ====================================================
    // INNER CLASS → CHANNEL BLOCK
    // ====================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Channel {

        public String channelNumber;
        public String temperature;
        public String status;

        public String setTemperature;

        public String highTemperature;
        public String highAlarm;

        public String lowTemperature;
        public String lowAlarm;
    }
}
