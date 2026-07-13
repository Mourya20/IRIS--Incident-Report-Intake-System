package com.iris.backend.controller;

import com.iris.backend.dto.IncidentDtos;
import com.iris.backend.enums.IncidentStatus;
import com.iris.backend.enums.SeverityBand;
import com.iris.backend.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentDtos.IncidentResponse createIncident(@Valid @RequestBody IncidentDtos.CreateIncidentRequest request) {
        return incidentService.create(request);
    }

    @GetMapping("/{id}")
    public IncidentDtos.IncidentResponse getById(@PathVariable Long id) {
        return incidentService.getById(id);
    }

    @GetMapping
    public List<IncidentDtos.IncidentResponse> list(@RequestParam(required = false) IncidentStatus status,
                                                    @RequestParam(required = false) SeverityBand severity,
                                                    @RequestParam(required = false) Long assigneeId) {
        return incidentService.list(status, severity, assigneeId);
    }

    @PatchMapping("/{id}/status")
    public IncidentDtos.IncidentResponse patchStatus(@PathVariable Long id,
                                                     @Valid @RequestBody IncidentDtos.UpdateStatusRequest request) {
        return incidentService.patchStatus(id, request);
    }

    @PostMapping("/{id}/iocs")
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentDtos.IocResponse addIoc(@PathVariable Long id,
                                           @Valid @RequestBody IncidentDtos.AddIocRequest request) {
        return incidentService.addIoc(id, request);
    }

    @GetMapping("/{id}/audit/verify")
    public IncidentDtos.AuditVerifyResponse verifyAudit(@PathVariable Long id) {
        return incidentService.verifyAudit(id);
    }
}
