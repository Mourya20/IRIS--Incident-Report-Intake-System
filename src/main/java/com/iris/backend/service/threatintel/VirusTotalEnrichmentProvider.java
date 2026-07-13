package com.iris.backend.service.threatintel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class VirusTotalEnrichmentProvider implements IOCEnrichmentProvider {

    private final boolean enabled;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public VirusTotalEnrichmentProvider(@Value("${threat-intel.virustotal.enabled:false}") boolean enabled,
                                        @Value("${threat-intel.virustotal.api-key:}") String apiKey,
                                        ObjectMapper objectMapper) {
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
    }

    @Override
    public String providerName() {
        return "VirusTotal";
    }

    @Override
    public Optional<IOCEnrichmentResult> enrich(String type, String value) {
        if (!enabled || apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }
        int score = Math.floorMod((type + value + "vt").hashCode(), 101);
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "provider", providerName(),
                    "status", "simulated",
                    "type", type,
                    "value", value,
                    "score", score));
            return Optional.of(new IOCEnrichmentResult(providerName(), score, payload));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
