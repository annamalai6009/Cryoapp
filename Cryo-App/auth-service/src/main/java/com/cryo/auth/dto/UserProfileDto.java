package com.cryo.auth.dto;

public class UserProfileDto {
    // Renamed 'userId' to 'ownerUserId' to match your Controller usage
    private String ownerUserId;
    private String email;
    private String mobileNumber;
    private String alternativeMobileNumber;

    // ✅ NEW: Notification Flags
    private Boolean notifyWhatsapp;
    private Boolean notifySms;
    private Boolean notifyEmail;

    public UserProfileDto() {
    }

    // ✅ UPDATED CONSTRUCTOR: Accepts all 7 fields
    public UserProfileDto(String ownerUserId, String email, String mobileNumber, String alternativeMobileNumber,
                          Boolean notifyWhatsapp, Boolean notifySms, Boolean notifyEmail) {
        this.ownerUserId = ownerUserId;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.alternativeMobileNumber = alternativeMobileNumber;
        this.notifyWhatsapp = notifyWhatsapp;
        this.notifySms = notifySms;
        this.notifyEmail = notifyEmail;
    }

    // ==========================
    // Getters and Setters
    // ==========================

    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getAlternativeMobileNumber() { return alternativeMobileNumber; }
    public void setAlternativeMobileNumber(String alternativeMobileNumber) { this.alternativeMobileNumber = alternativeMobileNumber; }

    public Boolean getNotifyWhatsapp() { return notifyWhatsapp; }
    public void setNotifyWhatsapp(Boolean notifyWhatsapp) { this.notifyWhatsapp = notifyWhatsapp; }

    public Boolean getNotifySms() { return notifySms; }
    public void setNotifySms(Boolean notifySms) { this.notifySms = notifySms; }

    public Boolean getNotifyEmail() { return notifyEmail; }
    public void setNotifyEmail(Boolean notifyEmail) { this.notifyEmail = notifyEmail; }
}