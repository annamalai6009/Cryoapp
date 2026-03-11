package com.cryo.alert.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FreezerStatusResponse {
    private String freezerId; // ✅ ADDED THIS
    private String name;
    private String poNumber;
    private BigDecimal currentTemp;
    private Boolean isFreezerOn;
    private Boolean isRedAlert;

    // Getters and Setters
    public String getFreezerId() { return freezerId; }
    public void setFreezerId(String freezerId) { this.freezerId = freezerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public BigDecimal getCurrentTemp() { return currentTemp; }
    public void setCurrentTemp(BigDecimal currentTemp) { this.currentTemp = currentTemp; }

    public Boolean getIsFreezerOn() { return isFreezerOn; }
    public void setIsFreezerOn(Boolean isFreezerOn) { this.isFreezerOn = isFreezerOn; }

    public Boolean getIsRedAlert() { return isRedAlert; }
    public void setIsRedAlert(Boolean isRedAlert) { this.isRedAlert = isRedAlert; }
}