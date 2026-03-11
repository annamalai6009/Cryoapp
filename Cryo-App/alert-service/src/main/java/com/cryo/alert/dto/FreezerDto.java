// FreezerDto
package com.cryo.alert.dto;

public class FreezerDto {
    private String freezerId;
    private String name;

    public FreezerDto() {
    }

    public FreezerDto(String freezerId, String name) {
        this.freezerId = freezerId;
        this.name = name;
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

}
