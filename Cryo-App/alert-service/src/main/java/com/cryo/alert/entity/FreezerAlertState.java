package com.cryo.alert.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "freezer_alert_states")
public class FreezerAlertState {

    @Id
    @Column(name = "freezer_id", nullable = false)
    private String freezerId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @Column(name = "is_acknowledged", nullable = false)
    private Boolean isAcknowledged = false;

    @Column(name = "last_alert_time")
    private LocalDateTime lastAlertTime;

    public FreezerAlertState() {}

    public FreezerAlertState(String freezerId) {
        this.freezerId = freezerId;
        this.isActive = false;
        this.isAcknowledged = false;
    }

    public String getFreezerId() { return freezerId; }
    public void setFreezerId(String freezerId) { this.freezerId = freezerId; }

    public Boolean getActive() { return isActive; }
    public void setActive(Boolean active) { isActive = active; }

    public Boolean getAcknowledged() { return isAcknowledged; }
    public void setAcknowledged(Boolean acknowledged) { isAcknowledged = acknowledged; }

    public LocalDateTime getLastAlertTime() { return lastAlertTime; }
    public void setLastAlertTime(LocalDateTime lastAlertTime) { this.lastAlertTime = lastAlertTime; }
}