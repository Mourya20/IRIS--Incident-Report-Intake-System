package com.iris.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iris.backend.entity.Incident;
import com.iris.backend.entity.SeverityRule;
import com.iris.backend.enums.SeverityBand;
import com.iris.backend.repository.SeverityRuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class SeverityScoringService {

    public record SeverityResult(int score, SeverityBand band) {}

    private final SeverityRuleRepository severityRuleRepository;
    private final ObjectMapper objectMapper;

    public SeverityScoringService(SeverityRuleRepository severityRuleRepository, ObjectMapper objectMapper) {
        this.severityRuleRepository = severityRuleRepository;
        this.objectMapper = objectMapper;
    }

    public SeverityResult score(Incident incident) {
        List<SeverityRule> rules = severityRuleRepository.findByEnabledTrue();
        String text = (incident.getTitle() + " " + incident.getDescription()).toLowerCase(Locale.ROOT);
        int score = 0;
        for (SeverityRule rule : rules) {
            if (matches(text, rule.getMetadataJson())) {
                score += rule.getWeight();
            }
        }
        score += incident.getTechniques().size() * 5;
        return new SeverityResult(score, mapBand(score));
    }

    private boolean matches(String text, String metadataJson) {
        try {
            JsonNode node = objectMapper.readTree(metadataJson);
            JsonNode keywords = node.get("keywords");
            if (keywords != null && keywords.isArray()) {
                for (JsonNode keyword : keywords) {
                    if (text.contains(keyword.asText().toLowerCase(Locale.ROOT))) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private SeverityBand mapBand(int score) {
        if (score >= 75) {
            return SeverityBand.CRITICAL;
        }
        if (score >= 50) {
            return SeverityBand.HIGH;
        }
        if (score >= 20) {
            return SeverityBand.MEDIUM;
        }
        return SeverityBand.LOW;
    }
}
