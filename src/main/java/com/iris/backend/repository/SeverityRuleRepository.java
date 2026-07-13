package com.iris.backend.repository;

import com.iris.backend.entity.SeverityRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeverityRuleRepository extends JpaRepository<SeverityRule, Long> {
    List<SeverityRule> findByEnabledTrue();
}
