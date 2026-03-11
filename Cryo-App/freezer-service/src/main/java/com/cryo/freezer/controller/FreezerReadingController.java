package com.cryo.freezer.controller;

import com.cryo.freezer.entity.FreezerReading;
import com.cryo.freezer.service.FreezerReadingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/readings")
public class FreezerReadingController {

    private final FreezerReadingService freezerReadingService;

    public FreezerReadingController(FreezerReadingService freezerReadingService) {
        this.freezerReadingService = freezerReadingService;
    }

    @PostMapping
    public ResponseEntity<FreezerReading> createReading(@RequestBody FreezerReading reading) {
        FreezerReading saved = freezerReadingService.saveReading(reading);
        return ResponseEntity.ok(saved);
    }
}
