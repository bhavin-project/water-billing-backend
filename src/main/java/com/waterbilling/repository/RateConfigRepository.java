package com.waterbilling.repository;

import com.waterbilling.entity.RateConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RateConfigRepository extends JpaRepository<RateConfig, Long> {
    List<RateConfig> findAllByOrderByEffectiveFromDesc();
    Optional<RateConfig> findByActiveTrue();

    List<RateConfig> findByEffectiveFromLessThanEqualAndActiveTrueOrderByEffectiveFromDesc(LocalDate date);
}