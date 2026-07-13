package com.iris.backend;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JwtAuthIntegrationTest extends AbstractIntegrationTest {

    @Test
    void registerAndLoginShouldIssueJwt() throws Exception {
        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "reporter1",
                                  "email": "reporter1@example.com",
                                  "password": "Password@123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode registerJson = objectMapper.readTree(registerResponse);
        assertThat(registerJson.get("token").asText()).isNotBlank();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "reporter1",
                                  "password": "Password@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        assertThat(loginJson.get("token").asText()).isNotBlank();
    }
}
