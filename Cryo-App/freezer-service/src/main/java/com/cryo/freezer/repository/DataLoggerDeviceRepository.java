package com.cryo.freezer.repository;

import com.cryo.freezer.entity.DataLoggerDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DataLoggerDeviceRepository
        extends JpaRepository<DataLoggerDevice, String> {

    Optional<DataLoggerDevice> findByPoNumber(String poNumber);
}
