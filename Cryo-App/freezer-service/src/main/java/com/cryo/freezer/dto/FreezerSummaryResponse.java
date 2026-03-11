package com.cryo.freezer.dto;

public class FreezerSummaryResponse {

    // 🔹 NORMAL FREEZER SUMMARY
    private long totalFreezers;
    private long activeFreezersCount;
    private long freezersOnCount;
    private long freezersOffCount;
    private long redAlertFreezersCount;

    // 🔹 DATA LOGGER SUMMARY
    private long totalDataLoggers;
    private long totalChannels;
    private long channelsSending;
    private long channelsNotSending;
    private long channelsInAlert;

    // 🔹 COMBINED
    // Total devices that are actively monitored (controllers + data loggers)
    private long activeDevicesCount;

    public FreezerSummaryResponse() {
    }

    public FreezerSummaryResponse(
            long totalFreezers,
            long activeFreezersCount,
            long freezersOnCount,
            long freezersOffCount,
            long redAlertFreezersCount,
            long totalDataLoggers,
            long totalChannels,
            long channelsSending,
            long channelsNotSending,
            long channelsInAlert,
            long activeDevicesCount
    ) {
        this.totalFreezers = totalFreezers;
        this.activeFreezersCount = activeFreezersCount;
        this.freezersOnCount = freezersOnCount;
        this.freezersOffCount = freezersOffCount;
        this.redAlertFreezersCount = redAlertFreezersCount;
        this.totalDataLoggers = totalDataLoggers;
        this.totalChannels = totalChannels;
        this.channelsSending = channelsSending;
        this.channelsNotSending = channelsNotSending;
        this.channelsInAlert = channelsInAlert;
        this.activeDevicesCount = activeDevicesCount;
    }

    public long getTotalFreezers() {
        return totalFreezers;
    }

    public void setTotalFreezers(long totalFreezers) {
        this.totalFreezers = totalFreezers;
    }

    public long getActiveFreezersCount() {
        return activeFreezersCount;
    }

    public void setActiveFreezersCount(long activeFreezersCount) {
        this.activeFreezersCount = activeFreezersCount;
    }

    public long getFreezersOnCount() {
        return freezersOnCount;
    }

    public void setFreezersOnCount(long freezersOnCount) {
        this.freezersOnCount = freezersOnCount;
    }

    public long getFreezersOffCount() {
        return freezersOffCount;
    }

    public void setFreezersOffCount(long freezersOffCount) {
        this.freezersOffCount = freezersOffCount;
    }

    public long getRedAlertFreezersCount() {
        return redAlertFreezersCount;
    }

    public void setRedAlertFreezersCount(long redAlertFreezersCount) {
        this.redAlertFreezersCount = redAlertFreezersCount;
    }

    public long getTotalDataLoggers() {
        return totalDataLoggers;
    }

    public void setTotalDataLoggers(long totalDataLoggers) {
        this.totalDataLoggers = totalDataLoggers;
    }

    public long getTotalChannels() {
        return totalChannels;
    }

    public void setTotalChannels(long totalChannels) {
        this.totalChannels = totalChannels;
    }

    public long getChannelsSending() {
        return channelsSending;
    }

    public void setChannelsSending(long channelsSending) {
        this.channelsSending = channelsSending;
    }

    public long getChannelsNotSending() {
        return channelsNotSending;
    }

    public void setChannelsNotSending(long channelsNotSending) {
        this.channelsNotSending = channelsNotSending;
    }

    public long getChannelsInAlert() {
        return channelsInAlert;
    }

    public void setChannelsInAlert(long channelsInAlert) {
        this.channelsInAlert = channelsInAlert;
    }

    public long getActiveDevicesCount() {
        return activeDevicesCount;
    }

    public void setActiveDevicesCount(long activeDevicesCount) {
        this.activeDevicesCount = activeDevicesCount;
    }
}
