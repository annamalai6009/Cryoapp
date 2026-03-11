package com.cryo.auth.dto;

public class UpdateProfileRequest {
    private String mobileNumber;
    private String alternativeMobileNumber;
    private Boolean notifyWhatsapp;
    private Boolean notifySms;
    private Boolean notifyEmail;

    public Boolean getNotifyWhatsapp() { return notifyWhatsapp; }
    public void setNotifyWhatsapp(Boolean notifyWhatsapp) { this.notifyWhatsapp = notifyWhatsapp; }

    public Boolean getNotifySms() { return notifySms; }
    public void setNotifySms(Boolean notifySms) { this.notifySms = notifySms; }

    public Boolean getNotifyEmail() { return notifyEmail; }
    public void setNotifyEmail(Boolean notifyEmail) { this.notifyEmail = notifyEmail; }

    // Getters and Setters
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getAlternativeMobileNumber() { return alternativeMobileNumber; }
    public void setAlternativeMobileNumber(String alternativeMobileNumber) { this.alternativeMobileNumber = alternativeMobileNumber; }
}