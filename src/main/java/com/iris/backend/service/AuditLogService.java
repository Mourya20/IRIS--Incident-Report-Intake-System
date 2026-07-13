package com.iris.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iris.backend.dto.IncidentDtos;
import com.iris.backend.entity.AppUser;
import com.iris.backend.entity.AuditLog;
import com.iris.backend.entity.Incident;
import com.iris.backend.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
public class AuditLogService {

    public static final String GENESIS_HASH = "GENESIS";

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditLogService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AuditLog append(Incident incident, AppUser actor, String actionType, Map<String, Object> details) {
        String detailsJson;
        try {
            detailsJson = objectMapper.writeValueAsString(details);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to serialize audit details");
        }

        String prevHash = incident == null ? GENESIS_HASH : auditLogRepository.findTopByIncidentIdOrderByIdDesc(incident.getId())
                .map(AuditLog::getEntryHash)
                .orElse(GENESIS_HASH);
        AuditLog log = new AuditLog();
        log.setIncident(incident);
        log.setActorUser(actor);
        log.setActionType(actionType);
        log.setDetailsJson(detailsJson);
        log.setPrevHash(prevHash);
        log.setEntryHash("PENDING");
        AuditLog saved = auditLogRepository.save(log);
        String payload = buildCanonicalPayload(incident == null ? null : incident.getId(), actor.getId(), actionType, detailsJson, saved.getCreatedAt(), prevHash);
        saved.setEntryHash(sha256(payload));
        return auditLogRepository.save(saved);
    }

    public IncidentDtos.AuditVerifyResponse verifyIncidentChain(Long incidentId) {
        List<AuditLog> logs = auditLogRepository.findByIncidentIdOrderByIdAsc(incidentId);
        String expectedPrev = GENESIS_HASH;
        for (int i = 0; i < logs.size(); i++) {
            AuditLog log = logs.get(i);
            if (!expectedPrev.equals(log.getPrevHash())) {
                return new IncidentDtos.AuditVerifyResponse(false, i);
            }
            String payload = buildCanonicalPayload(
                    log.getIncident() == null ? null : log.getIncident().getId(),
                    log.getActorUser().getId(),
                    log.getActionType(),
                    log.getDetailsJson(),
                    log.getCreatedAt(),
                    log.getPrevHash());
            String expectedHash = sha256(payload);
            if (!expectedHash.equals(log.getEntryHash())) {
                return new IncidentDtos.AuditVerifyResponse(false, i);
            }
            expectedPrev = log.getEntryHash();
        }
        return new IncidentDtos.AuditVerifyResponse(true, null);
    }

    private String buildCanonicalPayload(Long incidentId, Long actorId, String actionType, String detailsJson,
                                         Instant timestamp, String prevHash) {
        String normalizedDetails = detailsJson;
        try {
            normalizedDetails = objectMapper.readTree(detailsJson).toString();
        } catch (Exception ignored) {
        }
        return String.join("|",
                String.valueOf(incidentId),
                String.valueOf(actorId),
                actionType,
                normalizedDetails,
                timestamp.truncatedTo(ChronoUnit.MILLIS).toString(),
                prevHash);
    }

    private String sha256(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to calculate hash", e);
        }
    }
}
