package com.cryo.alert.repository;

import com.cryo.alert.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    @Query("SELECT a FROM Alert a WHERE a.freezerId = :freezerId AND a.timestamp BETWEEN :from AND :to ORDER BY a.timestamp DESC")
    List<Alert> findByFreezerIdAndTimestampBetween(
            @Param("freezerId") String freezerId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
