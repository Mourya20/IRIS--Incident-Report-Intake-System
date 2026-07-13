package com.iris.backend.dto;

import com.iris.backend.enums.IncidentStatus;
import com.iris.backend.enums.SeverityBand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public class IncidentDtos {

    public record CreateIncidentRequest(
            @NotBlank String title,
            @NotBlank String description
    ) {}

    public record UpdateStatusRequest(@NotNull IncidentStatus status) {}

    public record AddIocRequest(
            @NotBlank String type,
            @NotBlank String value
    ) {}

    public record IncidentResponse(
            Long id,
            String title,
            String description,
            IncidentStatus status,
            SeverityBand severityBand,
            Integer severityScore,
            Long reporterId,
            Long assigneeId,
            List<String> techniques,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record IocResponse(
            Long id,
            String type,
            String value,
            Integer normalizedScore,
            String sourceProvider,
            String rawResponseJson,
            Instant createdAt
    ) {}

    public record AuditVerifyResponse(boolean valid, Integer firstBrokenIndex) {}
}
