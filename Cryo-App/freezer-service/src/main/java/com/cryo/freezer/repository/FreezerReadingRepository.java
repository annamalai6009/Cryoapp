package com.cryo.freezer.repository;
import com.cryo.freezer.entity.FreezerReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FreezerReadingRepository extends JpaRepository<FreezerReading, Long> {

    // Get the latest reading for a freezer (ORDER BY timestamp DESC LIMIT 1)
    Optional<FreezerReading> findFirstByFreezerIdOrderByTimestampDesc(String freezerId);

    // Get readings in a range, sorted by time ASC
    List<FreezerReading> findByFreezerIdAndTimestampBetweenOrderByTimestampAsc(
            String freezerId,
            LocalDateTime from,
            LocalDateTime to
    );

    // All readings for a freezer, newest first (if you need it)
    List<FreezerReading> findByFreezerIdOrderByTimestampDesc(String freezerId);

    Optional<FreezerReading> findTopByFreezerIdOrderByTimestampDesc(String freezerId);

    @Query(value = """
        SELECT fr.* FROM freezer_readings fr
        INNER JOIN (
            SELECT freezer_id, MAX(timestamp) as max_ts
            FROM freezer_readings
            WHERE freezer_id IN (:freezerIds)
            GROUP BY freezer_id
        ) latest ON fr.freezer_id = latest.freezer_id AND fr.timestamp = latest.max_ts
        """, nativeQuery = true)
    List<FreezerReading> findLatestReadingsForFreezers(@Param("freezerIds") List<String> freezerIds);



}
