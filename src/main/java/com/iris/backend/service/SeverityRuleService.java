package com.iris.backend.service;

import com.iris.backend.dto.SeverityRuleDtos;
import com.iris.backend.repository.SeverityRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SeverityRuleService {

    private final SeverityRuleRepository severityRuleRepository;

    public SeverityRuleService(SeverityRuleRepository severityRuleRepository) {
        this.severityRuleRepository = severityRuleRepository;
    }

    public List<SeverityRuleDtos.SeverityRuleResponse> list() {
        return severityRuleRepository.findAll().stream()
                .map(rule -> new SeverityRuleDtos.SeverityRuleResponse(
                        rule.getId(), rule.getRuleKey(), rule.getWeight(), rule.isEnabled(), rule.getMetadataJson()
                ))
                .toList();
    }

    @Transactional
    public SeverityRuleDtos.SeverityRuleResponse update(Long id, SeverityRuleDtos.UpdateSeverityRuleRequest request) {
        var rule = severityRuleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Severity rule not found"));
        rule.setWeight(request.weight());
        rule.setEnabled(request.enabled());
        rule.setMetadataJson(request.metadataJson());
        var saved = severityRuleRepository.save(rule);
        return new SeverityRuleDtos.SeverityRuleResponse(
                saved.getId(), saved.getRuleKey(), saved.getWeight(), saved.isEnabled(), saved.getMetadataJson()
        );
    }
}
