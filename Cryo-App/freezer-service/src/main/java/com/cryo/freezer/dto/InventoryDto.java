package com.cryo.freezer.dto;

import com.cryo.freezer.entity.Freezer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class InventoryDto {

    @NotBlank(message = "PO Number is required")
    private String poNumber;

    @NotBlank(message = "S3 URL is required")
    private String s3Url;
    // ✅ ADD THIS
    @NotNull(message = "Device type is required")
    private Freezer.DeviceType deviceType;


    public InventoryDto() {
    }

    public InventoryDto(String poNumber, String s3Url, Freezer.DeviceType deviceType) {
        this.poNumber = poNumber;
        this.s3Url = s3Url;
        this.deviceType = deviceType;

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
    public Freezer.DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Freezer.DeviceType deviceType) {
        this.deviceType = deviceType;
    }
}