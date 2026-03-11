package com.cryo.freezer.dto;

public class FreezerResponse {
    private Long id;          // DB primary key
    private String freezerId; // business freezer id (what ESP32 uses)
    private String name;
    private String status;
    private String deviceType;   // ✅ NEW FIELD


    public FreezerResponse() {
    }

    public FreezerResponse(Long id, String freezerId, String name, String status, String deviceType) {
        this.id = id;
        this.freezerId = freezerId;
        this.name = name;
        this.status = status;
        this.deviceType = deviceType;


    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFreezerId() {
        return freezerId;
    }

    public void setFreezerId(String freezerId) {
        this.freezerId = freezerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
