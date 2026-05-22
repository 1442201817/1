package com.envmonitor.repository;

import com.envmonitor.entity.MonitorStation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<MonitorStation, Long> {
}
