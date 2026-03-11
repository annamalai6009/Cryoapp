package com.cryo.freezer.controller;
import com.cryo.common.dto.ApiResponse;
import com.cryo.freezer.dto.*;
import com.cryo.freezer.entity.Freezer;
import com.cryo.freezer.service.FreezerService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
@RestController
@RequestMapping("/freezers")
public class FreezerController {
    private final FreezerService freezerService;

    public FreezerController(FreezerService freezerService) {
        this.freezerService = freezerService;
    }

    // ✅ NEW: use X-User-Id header for current logged-in user
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Freezer>> registerFreezer(
            // JWT Filter validates the token; Gateway or Filter ensures X-User-Id is present.
            @RequestHeader("X-User-Id") String ownerUserId,
            @Valid @RequestBody FreezerRegisterRequest request) {

        Freezer newFreezer = freezerService.registerNewFreezer(ownerUserId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Freezer registered successfully.", newFreezer));
    }


//    // ✅ NEW ENDPOINT: Update Settings
//    @PutMapping("/{freezerId}/settings")
//    public ResponseEntity<ApiResponse<Void>> updateSettings(
//            @RequestHeader("X-User-Id") String ownerUserId,
//            @PathVariable("freezerId") String freezerId,
//            @Valid @RequestBody FreezerSettingsRequest request) {
//
//        freezerService.updateFreezerSettings(freezerId, ownerUserId, request);
//
//        return ResponseEntity.ok(ApiResponse.success("Settings updated successfully.", null));
//    }



    @GetMapping
    public ResponseEntity<ApiResponse<List<FreezerResponse>>> getAllFreezersForCurrentUser(@RequestHeader("X-User-Id") String ownerUserId)
    {

        List<FreezerResponse> freezers = freezerService.getAllFreezersForUser(ownerUserId);
        return ResponseEntity.ok(ApiResponse.success(freezers));
    }



    // ✅ NEW: summary for current logged-in user
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<FreezerSummaryResponse>> getFreezerSummaryForCurrentUser(
            @RequestHeader("X-User-Id") String ownerUserId) {

        FreezerSummaryResponse summary = freezerService.getFreezerSummary(ownerUserId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }


    // 🔁 OLD endpoints – keep if you still want to support /freezers/{ownerUserId}
    @GetMapping("/{ownerUserId}")
    public ResponseEntity<ApiResponse<List<FreezerResponse>>> getAllFreezers(
            @PathVariable("ownerUserId") String ownerUserId) {

        List<FreezerResponse> freezers = freezerService.getAllFreezersForUser(ownerUserId);
        return ResponseEntity.ok(ApiResponse.success(freezers));
    }


    @GetMapping("/summary/{ownerUserId}")
    public ResponseEntity<ApiResponse<FreezerSummaryResponse>> getFreezerSummary(
            @PathVariable("ownerUserId") String ownerUserId) {

        FreezerSummaryResponse summary = freezerService.getFreezerSummary(ownerUserId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }


    @GetMapping("/{freezerId}/status")
    public ResponseEntity<ApiResponse<FreezerStatusResponse>> getFreezerStatus(
            @PathVariable("freezerId") String freezerId) {
        FreezerStatusResponse status = freezerService.getFreezerStatus(freezerId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }


    @GetMapping("/{freezerId}/chart")
    public ResponseEntity<ApiResponse<List<?>>> getFreezerChart(
            @PathVariable("freezerId") String freezerId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(value = "channel", required = false) String channel) {

        List<?> chartData =
                freezerService.getFreezerChartData(freezerId, from, to, channel);

        return ResponseEntity.ok(ApiResponse.success(chartData));
    }

    @GetMapping("/{freezerId}/channels/latest")
    public ResponseEntity<ApiResponse<DataLoggerSnapshotResponse>> getLatestChannelSnapshot(
            @PathVariable("freezerId") String freezerId) {

        DataLoggerSnapshotResponse response =
                freezerService.getLatestChannelSnapshot(freezerId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }





    // ✅ NEW: Internal API for Chatbot
    @GetMapping("/api/internal/{ownerUserId}")
    public ResponseEntity<List<FreezerDetailResponse>> getInternalDashboard(
            @PathVariable("ownerUserId") String ownerUserId) {
        List<FreezerDetailResponse> details = freezerService.getFreezerDetailsForUser(ownerUserId);
        return ResponseEntity.ok(details);
    }



    @GetMapping("/{freezerId}/config")
    public ResponseEntity<ApiResponse<FreezerConfigResponse>> getFreezerConfig(
            @PathVariable("freezerId") String freezerId) {

        FreezerConfigResponse config = freezerService.getFreezerConfig(freezerId);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

}