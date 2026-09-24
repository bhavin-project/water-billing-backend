package com.waterbilling.repository;

import com.waterbilling.entity.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {

    Optional<MeterReading> findByUnitIdAndQuarterId(Long unitId, Long quarterId);

    List<MeterReading> findByQuarterId(Long quarterId);

    @Query("SELECT mr FROM MeterReading mr JOIN FETCH mr.unit u JOIN FETCH u.block " +
           "WHERE mr.quarter.id = :quarterId ORDER BY u.block.blockName, u.unitNumber")
    List<MeterReading> findByQuarterIdWithUnit(@Param("quarterId") Long quarterId);

    @Query("SELECT mr FROM MeterReading mr WHERE mr.unit.id = :unitId " +
           "ORDER BY mr.quarter.year DESC, mr.quarter.quarterNumber DESC")
    List<MeterReading> findByUnitIdOrderByQuarterDesc(@Param("unitId") Long unitId);

    @Query("SELECT mr FROM MeterReading mr WHERE mr.unit.id = :unitId " +
           "ORDER BY mr.quarter.year DESC, mr.quarter.quarterNumber DESC LIMIT 1")
    Optional<MeterReading> findLatestByUnitId(@Param("unitId") Long unitId);

    @Query("SELECT COUNT(mr) FROM MeterReading mr WHERE mr.quarter.id = :quarterId")
    Long countByQuarterId(@Param("quarterId") Long quarterId);

    @Query("SELECT mr FROM MeterReading mr JOIN FETCH mr.unit u JOIN FETCH u.block " +
           "WHERE mr.quarter.id = :quarterId AND u.block.id = :blockId ORDER BY u.unitNumber")
    List<MeterReading> findByQuarterIdAndBlockId(@Param("quarterId") Long quarterId,
                                                  @Param("blockId") Long blockId);
}