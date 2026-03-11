package com.cryo.freezer.scheduler;

import com.cryo.freezer.service.RealtimeS3FetchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RealtimeScheduler {

    private final RealtimeS3FetchService fetchService;

    public RealtimeScheduler(RealtimeS3FetchService fetchService) {
        this.fetchService = fetchService;
    }

    @Scheduled(fixedRate = 10000)
    public void pollAllDevices() {
        fetchService.fetchAllActiveFreezers();
    }
}