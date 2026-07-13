package com.iris.backend;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuditChainIntegrityTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void verifyEndpointShouldDetectBrokenChain() throws Exception {
        String token = registerAndGetToken("reporter2", "reporter2@example.com", "Password@123");

        String incidentResponse = mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "phishing attempt",
                                  "description": "Employee clicked suspicious link"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode incidentJson = objectMapper.readTree(incidentResponse);
        long incidentId = incidentJson.get("id").asLong();

        String verifyResponse = mockMvc.perform(get("/api/incidents/{id}/audit/verify", incidentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(verifyResponse).get("valid").asBoolean()).isTrue();

        jdbcTemplate.update("UPDATE audit_log SET details_json = ? WHERE incident_id = ?", "{\"tampered\":true}", incidentId);

        String verifyAfterTamper = mockMvc.perform(get("/api/incidents/{id}/audit/verify", incidentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode tamperedJson = objectMapper.readTree(verifyAfterTamper);
        assertThat(tamperedJson.get("valid").asBoolean()).isFalse();
        assertThat(tamperedJson.get("firstBrokenIndex").asInt()).isZero();
    }
}
