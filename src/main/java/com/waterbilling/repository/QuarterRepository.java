package com.waterbilling.repository;

import com.waterbilling.entity.Quarter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuarterRepository extends JpaRepository<Quarter, Long> {
    Optional<Quarter> findByYearAndQuarterNumber(Integer year, Integer quarterNumber);
    List<Quarter> findAllByOrderByYearDescQuarterNumberDesc();
    List<Quarter> findByClosedFalse();
}