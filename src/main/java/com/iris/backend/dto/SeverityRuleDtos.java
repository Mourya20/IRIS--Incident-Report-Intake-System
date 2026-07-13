package com.iris.backend.dto;

import jakarta.validation.constraints.NotNull;

public class SeverityRuleDtos {

    public record SeverityRuleResponse(Long id, String ruleKey, Integer weight, boolean enabled, String metadataJson) {}

    public record UpdateSeverityRuleRequest(
            @NotNull Integer weight,
            @NotNull Boolean enabled,
            @NotNull String metadataJson
    ) {}
}
