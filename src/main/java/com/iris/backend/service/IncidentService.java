package com.iris.backend.service;

import com.iris.backend.dto.IncidentDtos;
import com.iris.backend.entity.AppUser;
import com.iris.backend.entity.Incident;
import com.iris.backend.entity.Ioc;
import com.iris.backend.enums.IncidentStatus;
import com.iris.backend.enums.RoleName;
import com.iris.backend.enums.SeverityBand;
import com.iris.backend.repository.IncidentRepository;
import com.iris.backend.service.threatintel.IOCEnrichmentService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class IncidentService {

    private static final Set<IncidentStatus> OPEN_STATUSES = EnumSet.of(IncidentStatus.NEW, IncidentStatus.TRIAGED, IncidentStatus.IN_PROGRESS);

    private final IncidentRepository incidentRepository;
    private final CurrentUserService currentUserService;
    private final AccessControlService accessControlService;
    private final MitreTechniqueSuggestionService mitreTechniqueSuggestionService;
    private final SeverityScoringService severityScoringService;
    private final IOCEnrichmentService iocEnrichmentService;
    private final AuditLogService auditLogService;

    public IncidentService(IncidentRepository incidentRepository,
                           CurrentUserService currentUserService,
                           AccessControlService accessControlService,
                           MitreTechniqueSuggestionService mitreTechniqueSuggestionService,
                           SeverityScoringService severityScoringService,
                           IOCEnrichmentService iocEnrichmentService,
                           AuditLogService auditLogService) {
        this.incidentRepository = incidentRepository;
        this.currentUserService = currentUserService;
        this.accessControlService = accessControlService;
        this.mitreTechniqueSuggestionService = mitreTechniqueSuggestionService;
        this.severityScoringService = severityScoringService;
        this.iocEnrichmentService = iocEnrichmentService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public IncidentDtos.IncidentResponse create(IncidentDtos.CreateIncidentRequest request) {
        AppUser user = currentUserService.requireCurrentUser();
        if (!accessControlService.hasRole(user, RoleName.REPORTER) && !accessControlService.hasRole(user, RoleName.ADMIN)) {
            throw new AccessDeniedException("Only reporter/admin can create incidents");
        }

        Incident incident = new Incident();
        incident.setTitle(request.title());
        incident.setDescription(request.description());
        incident.setReporter(user);
        incident.getTechniques().addAll(mitreTechniqueSuggestionService.suggestTechniques(request.title(), request.description()));

        var severity = severityScoringService.score(incident);
        incident.setSeverityScore(severity.score());
        incident.setSeverityBand(severity.band());

        Incident saved = incidentRepository.save(incident);
        auditLogService.append(saved, user, "INCIDENT_CREATED", Map.of("title", saved.getTitle(), "status", saved.getStatus().name()));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public IncidentDtos.IncidentResponse getById(Long id) {
        AppUser user = currentUserService.requireCurrentUser();
        Incident incident = incidentRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Incident not found"));
        accessControlService.canViewIncident(user, incident);
        return toResponse(incident);
    }

    @Transactional(readOnly = true)
    public List<IncidentDtos.IncidentResponse> list(IncidentStatus status, SeverityBand severityBand, Long assigneeId) {
        AppUser user = currentUserService.requireCurrentUser();
        Specification<Incident> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (accessControlService.hasRole(user, RoleName.REPORTER)) {
                predicates.add(cb.equal(root.get("reporter").get("id"), user.getId()));
            } else if (accessControlService.hasRole(user, RoleName.ANALYST) && !accessControlService.hasRole(user, RoleName.ADMIN)) {
                predicates.add(cb.or(
                        cb.equal(root.get("assignee").get("id"), user.getId()),
                        root.get("status").in(OPEN_STATUSES)
                ));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (severityBand != null) {
                predicates.add(cb.equal(root.get("severityBand"), severityBand));
            }
            if (assigneeId != null) {
                predicates.add(cb.equal(root.get("assignee").get("id"), assigneeId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return incidentRepository.findAll(spec).stream().map(this::toResponse).toList();
    }

    @Transactional
    public IncidentDtos.IncidentResponse patchStatus(Long id, IncidentDtos.UpdateStatusRequest request) {
        AppUser user = currentUserService.requireCurrentUser();
        Incident incident = incidentRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Incident not found"));

        accessControlService.canUpdateIncidentStatus(user, incident);
        IncidentStatus previous = incident.getStatus();
        incident.setStatus(request.status());

        var severity = severityScoringService.score(incident);
        incident.setSeverityScore(severity.score());
        incident.setSeverityBand(severity.band());

        Incident saved = incidentRepository.save(incident);
        auditLogService.append(saved, user, "INCIDENT_STATUS_UPDATED", Map.of("from", previous.name(), "to", saved.getStatus().name()));

        return toResponse(saved);
    }

    @Transactional
    public IncidentDtos.IocResponse addIoc(Long id, IncidentDtos.AddIocRequest request) {
        AppUser user = currentUserService.requireCurrentUser();
        Incident incident = incidentRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Incident not found"));

        accessControlService.canAddIoc(user, incident);

        Ioc ioc = iocEnrichmentService.enrichAndPersist(incident, request.type(), request.value());
        auditLogService.append(incident, user, "IOC_ADDED", Map.of("iocId", ioc.getId(), "type", ioc.getType(), "value", ioc.getValue()));
        return toIocResponse(ioc);
    }

    @Transactional(readOnly = true)
    public IncidentDtos.AuditVerifyResponse verifyAudit(Long incidentId) {
        AppUser user = currentUserService.requireCurrentUser();
        Incident incident = incidentRepository.findById(incidentId).orElseThrow(() -> new NoSuchElementException("Incident not found"));
        accessControlService.canViewIncident(user, incident);
        return auditLogService.verifyIncidentChain(incidentId);
    }

    private IncidentDtos.IncidentResponse toResponse(Incident incident) {
        return new IncidentDtos.IncidentResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getStatus(),
                incident.getSeverityBand(),
                incident.getSeverityScore(),
                incident.getReporter() == null ? null : incident.getReporter().getId(),
                incident.getAssignee() == null ? null : incident.getAssignee().getId(),
                incident.getTechniques().stream().map(t -> t.getTechniqueId()).sorted().toList(),
                incident.getCreatedAt(),
                incident.getUpdatedAt()
        );
    }

    private IncidentDtos.IocResponse toIocResponse(Ioc ioc) {
        return new IncidentDtos.IocResponse(
                ioc.getId(),
                ioc.getType(),
                ioc.getValue(),
                ioc.getNormalizedScore(),
                ioc.getSourceProvider(),
                ioc.getRawResponseJson(),
                ioc.getCreatedAt()
        );
    }
}
