package com.iris.backend.service.threatintel;

public record IOCEnrichmentResult(String provider, int normalizedScore, String rawResponseJson) {
}
