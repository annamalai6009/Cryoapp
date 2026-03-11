package com.cryo.freezer.dto;

import java.math.BigDecimal;

public class FreezerConfigResponse {
    private String freezerId;
    private String name;
//    private BigDecimal minThreshold;
//    private BigDecimal maxThreshold;

    public FreezerConfigResponse(String freezerId, String name) {
        this.freezerId = freezerId;
        this.name = name;
//        this.minThreshold = minThreshold;
//        this.maxThreshold = maxThreshold;
    }

    // Getters
    public String getFreezerId() { return freezerId; }
    public String getName() { return name; }
//    public BigDecimal getMinThreshold() { return minThreshold; }
//    public BigDecimal getMaxThreshold() { return maxThreshold; }
}