package com.iris.backend.repository;

import com.iris.backend.entity.AttackTechnique;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttackTechniqueRepository extends JpaRepository<AttackTechnique, String> {
}
