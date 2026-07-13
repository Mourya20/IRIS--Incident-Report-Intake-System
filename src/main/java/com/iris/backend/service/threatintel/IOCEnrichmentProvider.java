package com.iris.backend.service.threatintel;

import java.util.Optional;

public interface IOCEnrichmentProvider {
    String providerName();

    Optional<IOCEnrichmentResult> enrich(String type, String value);
}
