package com.iris.backend.repository;

import com.iris.backend.entity.Ioc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IocRepository extends JpaRepository<Ioc, Long> {
    Optional<Ioc> findByIncidentIdAndTypeAndValue(Long incidentId, String type, String value);
}
