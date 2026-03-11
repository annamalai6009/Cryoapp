package com.cryo.freezer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "device_inventory")
public class DeviceInventory {

    @Id
    @Column(name = "po_number", nullable = false, unique = true)
    private String poNumber;

    @Column(name = "s3_url", nullable = false, length = 500)
    private String s3Url;

    @Column(name = "is_claimed")
    private Boolean isClaimed = false;


    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false)
    private Freezer.DeviceType deviceType;

    public enum DeviceType {
        NORMAL_FREEZER,
        DATA_LOGGER
    }



    public DeviceInventory() {
    }

    public DeviceInventory(String poNumber, String s3Url) {
        this.poNumber = poNumber;
        this.s3Url = s3Url;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public String getS3Url() {
        return s3Url;
    }

    public void setS3Url(String s3Url) {
        this.s3Url = s3Url;
    }

    public Boolean getIsClaimed() {
        return isClaimed;
    }

    public void setIsClaimed(Boolean claimed) {
        isClaimed = claimed;
    }

    public Freezer.DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Freezer.DeviceType deviceType) {
        this.deviceType = deviceType;
    }
}