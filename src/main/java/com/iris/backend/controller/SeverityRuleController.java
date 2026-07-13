package com.iris.backend.controller;

import com.iris.backend.dto.SeverityRuleDtos;
import com.iris.backend.service.SeverityRuleService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/severity-rules")
@PreAuthorize("hasRole('ADMIN')")
public class SeverityRuleController {

    private final SeverityRuleService severityRuleService;

    public SeverityRuleController(SeverityRuleService severityRuleService) {
        this.severityRuleService = severityRuleService;
    }

    @GetMapping
    public List<SeverityRuleDtos.SeverityRuleResponse> list() {
        return severityRuleService.list();
    }

    @PutMapping("/{id}")
    public SeverityRuleDtos.SeverityRuleResponse update(@PathVariable Long id,
                                                        @Valid @RequestBody SeverityRuleDtos.UpdateSeverityRuleRequest request) {
        return severityRuleService.update(id, request);
    }
}
