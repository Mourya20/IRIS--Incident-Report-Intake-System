package com.iris.backend.service.threatintel;

import com.iris.backend.entity.Ioc;
import com.iris.backend.entity.Incident;
import com.iris.backend.repository.IocRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class IOCEnrichmentService {

    private final List<IOCEnrichmentProvider> providers;
    private final IocRepository iocRepository;

    public IOCEnrichmentService(List<IOCEnrichmentProvider> providers, IocRepository iocRepository) {
        this.providers = providers;
        this.iocRepository = iocRepository;
    }

    @Transactional
    public Ioc enrichAndPersist(Incident incident, String type, String value) {
        return iocRepository.findByIncidentIdAndTypeAndValue(incident.getId(), type, value)
                .orElseGet(() -> doEnrichAndPersist(incident, type, value));
    }

    private Ioc doEnrichAndPersist(Incident incident, String type, String value) {
        List<IOCEnrichmentResult> results = new ArrayList<>();
        for (IOCEnrichmentProvider provider : providers) {
            try {
                provider.enrich(type, value).ifPresent(results::add);
            } catch (Exception ignored) {
            }
        }

        Ioc ioc = new Ioc();
        ioc.setIncident(incident);
        ioc.setType(type);
        ioc.setValue(value);

        if (!results.isEmpty()) {
            IOCEnrichmentResult primary = results.stream().max(Comparator.comparingInt(IOCEnrichmentResult::normalizedScore)).orElseThrow();
            ioc.setNormalizedScore(primary.normalizedScore());
            ioc.setRawResponseJson(primary.rawResponseJson());
            ioc.setSourceProvider(String.join(",", results.stream().map(IOCEnrichmentResult::provider).toList()));
        }

        return iocRepository.save(ioc);
    }
}
