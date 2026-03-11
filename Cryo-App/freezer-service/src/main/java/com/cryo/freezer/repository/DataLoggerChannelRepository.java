package com.cryo.freezer.repository;

import com.cryo.freezer.entity.DataLoggerChannelReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface   DataLoggerChannelRepository
        extends JpaRepository<DataLoggerChannelReading, Long> {


    @Query("""
SELECT c FROM DataLoggerChannelReading c
WHERE c.topic = :topic
AND c.timestamp = (
    SELECT MAX(c2.timestamp)
    FROM DataLoggerChannelReading c2
    WHERE c2.topic = :topic
    AND c2.channelNumber = c.channelNumber
)
""")
    List<DataLoggerChannelReading> findLatestChannelsByTopic(@Param("topic") String topic);

    List<DataLoggerChannelReading>
    findByTopicAndChannelNumberAndTimestampBetweenOrderByTimestampAsc(
            String topic,
            String channelNumber,
            LocalDateTime from,
            LocalDateTime to
    );

    List<DataLoggerChannelReading>
    findByTopicAndTimestampBetweenOrderByTimestampAsc(
            String topic,
            LocalDateTime from,
            LocalDateTime to
    );





}
