package com.envmonitor.repository;

import com.envmonitor.entity.EnvData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface EnvDataRepository extends JpaRepository<EnvData, Long> {

    EnvData findTopByStationIdOrderByRecordedAtDesc(Long stationId);

    List<EnvData> findByStationIdAndRecordedAtAfterOrderByRecordedAtAsc(Long stationId, LocalDateTime since);

    List<EnvData> findByStationIdOrderByRecordedAtDesc(Long stationId);

    void deleteByStationId(Long stationId);
}
