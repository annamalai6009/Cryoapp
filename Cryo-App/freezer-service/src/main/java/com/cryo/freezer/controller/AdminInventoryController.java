package com.cryo.freezer.controller;

import com.cryo.common.dto.ApiResponse;
import com.cryo.freezer.dto.InventoryDto;
import com.cryo.freezer.service.DeviceInventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/freezers/admin")
public class AdminInventoryController {

    private final DeviceInventoryService inventoryService;

    public AdminInventoryController(DeviceInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // ✅ Bulk upload devices (Admin Only)
    @PostMapping("/inventory")
    public ResponseEntity<ApiResponse<String>> addInventory(@RequestBody @Valid List<InventoryDto> request) {

        int count = inventoryService.bulkAddInventory(request);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Successfully added %d devices to inventory.", count)
        ));
    }
}