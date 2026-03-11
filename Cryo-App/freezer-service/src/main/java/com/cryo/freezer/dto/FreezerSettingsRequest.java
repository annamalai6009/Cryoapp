//package com.cryo.freezer.dto;
//
//import jakarta.validation.constraints.NotNull;
//import java.math.BigDecimal;
//
//public class FreezerSettingsRequest {
//
//    @NotNull(message = "Min threshold is required")
//    private BigDecimal minThreshold;
//
//    @NotNull(message = "Max threshold is required")
//    private BigDecimal maxThreshold;
//
//    // --- Getters and Setters ---
//    public BigDecimal getMinThreshold() {
//        return minThreshold;
//    }
//
//    public void setMinThreshold(BigDecimal minThreshold) {
//        this.minThreshold = minThreshold;
//    }
//
//    public BigDecimal getMaxThreshold() {
//        return maxThreshold;
//    }
//
//    public void setMaxThreshold(BigDecimal maxThreshold) {
//        this.maxThreshold = maxThreshold;
//    }
//}