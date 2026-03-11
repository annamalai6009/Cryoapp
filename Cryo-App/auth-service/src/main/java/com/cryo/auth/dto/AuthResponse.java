package com.cryo.auth.dto;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String ownerUserId;
    private String email;
    private String roles;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken,
                        String refreshToken,
                        String ownerUserId,
                        String email,
                        String roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.ownerUserId = ownerUserId;
        this.email = email;
        this.roles = roles;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
}
