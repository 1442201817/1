package com.envmonitor.repository;

import com.envmonitor.entity.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {
}
