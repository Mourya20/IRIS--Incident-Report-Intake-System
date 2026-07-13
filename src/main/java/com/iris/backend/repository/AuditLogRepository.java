package com.iris.backend.repository;

import com.iris.backend.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Optional<AuditLog> findTopByIncidentIdOrderByIdDesc(Long incidentId);

    List<AuditLog> findByIncidentIdOrderByIdAsc(Long incidentId);
}
