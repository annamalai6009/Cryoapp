package com.cryo.freezer.controller;

import com.cryo.common.dto.ApiResponse;
import com.cryo.freezer.entity.FreezerReading;
import com.cryo.freezer.repository.FreezerReadingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/freezers")
public class RealtimeController {

    private final FreezerReadingRepository freezerReadingRepository;

    public RealtimeController(FreezerReadingRepository freezerReadingRepository) {
        this.freezerReadingRepository = freezerReadingRepository;
    }

    /**
     * GET /freezers/{freezerId}/realtime
     * Returns latest reading if present.
     */
    @GetMapping("/{freezerId}/realtime")
    public ResponseEntity<ApiResponse<FreezerReading>> getLatestRealtime(@PathVariable("freezerId") String freezerId) {
        Optional<FreezerReading> opt = freezerReadingRepository.findFirstByFreezerIdOrderByTimestampDesc(freezerId);
        return opt
                .map(r -> ResponseEntity.ok(ApiResponse.success(r)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.success(null)));
    }
}
