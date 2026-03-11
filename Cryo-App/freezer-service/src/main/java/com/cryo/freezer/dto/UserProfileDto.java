package com.cryo.freezer.dto;
public class UserProfileDto {
    private String userId;
    private String email;
    private String mobileNumber;
    // ✅ Add these fields so Freezer Service can receive them from Auth Service
    private Boolean notifyWhatsapp;
    private Boolean notifySms;
    private Boolean notifyEmail;

    // Constructors
    public UserProfileDto() {
    }

    public UserProfileDto(String userId, String email, String mobileNumber,Boolean notifyWhatsapp,Boolean notifySms,Boolean notifyEmail) {
        this.userId = userId;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.notifyWhatsapp=notifyWhatsapp;
        this.notifySms=notifySms;
        this.notifyEmail=notifyEmail;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
    public Boolean getNotifyWhatsapp() { return notifyWhatsapp; }
    public void setNotifyWhatsapp(Boolean n) { this.notifyWhatsapp = n; }
    public Boolean getNotifySms() { return notifySms; }
    public void setNotifySms(Boolean n) { this.notifySms = n; }
    public Boolean getNotifyEmail() { return notifyEmail; }
    public void setNotifyEmail(Boolean n) { this.notifyEmail = n; }
}