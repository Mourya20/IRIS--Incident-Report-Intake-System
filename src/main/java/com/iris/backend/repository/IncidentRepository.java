package com.iris.backend.repository;

import com.iris.backend.entity.Incident;
import com.iris.backend.enums.IncidentStatus;
import com.iris.backend.enums.SeverityBand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {
    List<Incident> findByReporterId(Long reporterId);

    List<Incident> findByAssigneeIdOrStatusIn(Long assigneeId, List<IncidentStatus> statuses);

    List<Incident> findByStatusIn(List<IncidentStatus> statuses);

    List<Incident> findByStatusAndSeverityBandAndAssigneeId(IncidentStatus status, SeverityBand severityBand, Long assigneeId);
}
