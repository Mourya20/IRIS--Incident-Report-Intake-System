package com.iris.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.iris.backend.enums.RoleName;
import com.iris.backend.repository.AppUserRepository;
import com.iris.backend.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IncidentAuthorizationTest extends AbstractIntegrationTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void reporterCanCreateButCannotAddIocAnalystCanAddIoc() throws Exception {
        String reporterToken = registerAndGetToken("reporter3", "reporter3@example.com", "Password@123");
        registerAndGetToken("analyst1", "analyst1@example.com", "Password@123");

        var analyst = appUserRepository.findByUsername("analyst1").orElseThrow();
        analyst.getRoles().clear();
        analyst.getRoles().add(roleRepository.findByName(RoleName.ANALYST).orElseThrow());
        appUserRepository.save(analyst);
        String analystToken = loginAndGetToken("analyst1", "Password@123");

        String incidentResponse = mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "possible ransomware",
                                  "description": "files got encrypted"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode incidentJson = objectMapper.readTree(incidentResponse);
        long incidentId = incidentJson.get("id").asLong();

        mockMvc.perform(post("/api/incidents/{id}/iocs", incidentId)
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "IP",
                                  "value": "1.2.3.4"
                                }
                                """))
                .andExpect(status().isForbidden());

        String iocResponse = mockMvc.perform(post("/api/incidents/{id}/iocs", incidentId)
                        .header("Authorization", "Bearer " + analystToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "IP",
                                  "value": "1.2.3.4"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readTree(iocResponse).get("id").asLong()).isPositive();
    }
}
