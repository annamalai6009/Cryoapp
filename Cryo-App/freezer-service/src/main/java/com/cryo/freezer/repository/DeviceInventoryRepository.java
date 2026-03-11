package com.cryo.freezer.repository;

import com.cryo.freezer.entity.DeviceInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceInventoryRepository extends JpaRepository<DeviceInventory, String> {
    Optional<DeviceInventory> findByPoNumber(String poNumber);
}