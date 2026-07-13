package com.iris.backend.service;

import com.iris.backend.entity.AttackTechnique;
import com.iris.backend.repository.AttackTechniqueRepository;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MitreTechniqueSuggestionService {

    private final KeywordTechniqueProperties keywordTechniqueProperties;
    private final AttackTechniqueRepository attackTechniqueRepository;

    public MitreTechniqueSuggestionService(KeywordTechniqueProperties keywordTechniqueProperties,
                                           AttackTechniqueRepository attackTechniqueRepository) {
        this.keywordTechniqueProperties = keywordTechniqueProperties;
        this.attackTechniqueRepository = attackTechniqueRepository;
    }

    public Set<AttackTechnique> suggestTechniques(String title, String description) {
        String text = (title + " " + description).toLowerCase(Locale.ROOT);
        Set<String> ids = new HashSet<>();
        keywordTechniqueProperties.getKeywordTechniques().forEach((keyword, technique) -> {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                ids.add(technique);
            }
        });
        return new HashSet<>(attackTechniqueRepository.findAllById(ids));
    }

    @Component
    @ConfigurationProperties(prefix = "mitre")
    public static class KeywordTechniqueProperties {
        private Map<String, String> keywordTechniques = new HashMap<>();

        public Map<String, String> getKeywordTechniques() {
            return keywordTechniques;
        }

        public void setKeywordTechniques(Map<String, String> keywordTechniques) {
            this.keywordTechniques = keywordTechniques;
        }
    }
}
