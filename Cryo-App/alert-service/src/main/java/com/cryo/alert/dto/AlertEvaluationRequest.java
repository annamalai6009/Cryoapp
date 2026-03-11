package com.cryo.alert.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AlertEvaluationRequest {

    @NotBlank
    private String freezerId;

    @NotBlank
    private String ownerUserId;

    @NotNull
    private BigDecimal temperature;

    @NotNull
    private Boolean freezerOn;

    // ✅ FIX: REMOVED
    // @NotNull because Data Loggers have no door (value is null)
    private Boolean doorOpen;

    @NotNull
    private LocalDateTime timestamp;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String mobileNumber;

    @NotBlank
    private String freezerName;

    // ✅ NEW FIELDS
    @NotNull(message = "Min threshold required")
    private BigDecimal minThreshold;

    @NotNull(message = "Max threshold required")
    private BigDecimal maxThreshold;
    private Boolean notifyWhatsapp;
    private Boolean notifySms;
    private Boolean notifyEmail;

    // Getters and Setters
    public Boolean getNotifyWhatsapp() { return notifyWhatsapp; }
    public void setNotifyWhatsapp(Boolean n) { this.notifyWhatsapp = n; }

    public Boolean getNotifySms() { return notifySms; }
    public void setNotifySms(Boolean n) { this.notifySms = n; }

    public Boolean getNotifyEmail() { return notifyEmail; }
    public void setNotifyEmail(Boolean n) { this.notifyEmail = n; }

    // --- Getters and Setters ---

    public String getFreezerId() {
        return freezerId;
    }

    public void setFreezerId(String freezerId) {
        this.freezerId = freezerId;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getFreezerName() {
        return freezerName;
    }

    public void setFreezerName(String freezerName) {
        this.freezerName = freezerName;
    }

    // ✅ New Getters/Setters
    public BigDecimal getMinThreshold() { return minThreshold; }
    public void setMinThreshold(BigDecimal minThreshold) { this.minThreshold = minThreshold; }

    public BigDecimal getMaxThreshold() { return maxThreshold; }
    public void setMaxThreshold(BigDecimal maxThreshold) { this.maxThreshold = maxThreshold; }
}