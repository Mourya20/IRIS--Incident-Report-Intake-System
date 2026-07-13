package com.iris.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "attack_technique")
public class AttackTechnique {

    @Id
    @Column(name = "technique_id", length = 20)
    private String techniqueId;

    @Column(nullable = false, length = 100)
    private String tactic;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    public String getTechniqueId() {
        return techniqueId;
    }

    public String getTactic() {
        return tactic;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
