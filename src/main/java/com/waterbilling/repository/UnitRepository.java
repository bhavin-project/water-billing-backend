package com.waterbilling.repository;

import com.waterbilling.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {
    List<Unit> findByBlockIdAndActiveTrue(Long blockId);
    List<Unit> findByActiveTrue();
    Optional<Unit> findByUnitNumber(String unitNumber);

    @Query("SELECT u FROM Unit u JOIN FETCH u.block WHERE u.active = true ORDER BY u.block.blockName, u.unitNumber")
    List<Unit> findAllActiveWithBlock();
}