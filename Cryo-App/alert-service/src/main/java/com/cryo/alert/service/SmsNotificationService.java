package com.cryo.alert.service;

import com.cryo.alert.dto.FreezerDto;
import com.cryo.alert.dto.FreezerReadingDto;

public interface SmsNotificationService {
    void sendTemperatureAlert(String mobileNumber, FreezerDto freezer, FreezerReadingDto reading);
}

