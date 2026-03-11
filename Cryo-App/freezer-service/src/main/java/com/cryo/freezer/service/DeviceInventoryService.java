package com.cryo.freezer.service;
import com.cryo.freezer.dto.InventoryDto;
import com.cryo.freezer.entity.DeviceInventory;
import com.cryo.freezer.repository.DeviceInventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
@Service
public class DeviceInventoryService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceInventoryService.class);
    private final DeviceInventoryRepository repository;

    public DeviceInventoryService(DeviceInventoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public int bulkAddInventory(List<InventoryDto> dtos) {
        List<DeviceInventory> newDevices = new ArrayList<>();
        int skipped = 0;

        for (InventoryDto dto : dtos) {
            // Check if PO already exists to prevent errors
            if (repository.existsById(dto.getPoNumber())) {
                skipped++;
                continue;
            }

            DeviceInventory device = new DeviceInventory();
            device.setPoNumber(dto.getPoNumber());
            device.setS3Url(dto.getS3Url());
            device.setDeviceType(dto.getDeviceType()); // ✅ IMPORTANT
            device.setIsClaimed(false); // New devices are always unclaimed

            newDevices.add(device);
        }

        if (!newDevices.isEmpty()) {
            repository.saveAll(newDevices);
        }

        logger.info("Bulk upload: Added {} devices. Skipped {} duplicates.", newDevices.size(), skipped);
        return newDevices.size();
    }
}
