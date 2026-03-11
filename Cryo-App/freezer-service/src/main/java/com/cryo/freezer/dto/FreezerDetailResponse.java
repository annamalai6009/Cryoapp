package com.cryo.freezer.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public class FreezerDetailResponse {
    private String freezerId;
    private String name;
    private String poNumber;
    private String status;
    private BigDecimal currentTemp;
    private Boolean isFreezerOn;
    private Boolean isDoorOpen;
    private LocalDateTime lastUpdate;
    private Boolean isRedAlert;
    // ✅ NEW FIELD: Average Temperature
    private Double oneMinuteAvgTemp;

    public FreezerDetailResponse() {}

    public FreezerDetailResponse(String freezerId, String name, String poNumber, String status,
                                 BigDecimal currentTemp, Boolean isFreezerOn, Boolean isDoorOpen,
                                 LocalDateTime lastUpdate, Boolean isRedAlert,Double oneMinuteAvgTemp) {
        this.freezerId = freezerId;
        this.name = name;
        this.poNumber = poNumber;
        this.status = status;
        this.currentTemp = currentTemp;
        this.isFreezerOn = isFreezerOn;
        this.isDoorOpen = isDoorOpen;
        this.lastUpdate = lastUpdate;
        this.isRedAlert = isRedAlert;
        this.oneMinuteAvgTemp = oneMinuteAvgTemp;

    }
    // Getters
    public String getFreezerId() { return freezerId; }
    public String getName() { return name; }
    public String getPoNumber() { return poNumber; }
    public String getStatus() { return status; }
    public BigDecimal getCurrentTemp() { return currentTemp; }
    public Boolean getIsFreezerOn() { return isFreezerOn; }
    public Boolean getIsDoorOpen() { return isDoorOpen; }
    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public Boolean getIsRedAlert() { return isRedAlert; }
    // ✅ NEW GETTER/SETTER
    public Double getOneMinuteAvgTemp() { return oneMinuteAvgTemp; }
    public void setOneMinuteAvgTemp(Double oneMinuteAvgTemp) { this.oneMinuteAvgTemp = oneMinuteAvgTemp; }
}