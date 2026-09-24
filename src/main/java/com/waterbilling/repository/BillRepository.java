package com.waterbilling.repository;

import com.waterbilling.entity.Bill;
import com.waterbilling.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByUnitIdAndQuarterId(Long unitId, Long quarterId);

    Optional<Bill> findByBillNumber(String billNumber);

    List<Bill> findByQuarterId(Long quarterId);

    @Query("SELECT b FROM Bill b JOIN FETCH b.unit u JOIN FETCH u.block JOIN FETCH b.quarter " +
           "WHERE b.quarter.id = :quarterId ORDER BY u.block.blockName, u.unitNumber")
    List<Bill> findByQuarterIdWithDetails(@Param("quarterId") Long quarterId);

    @Query("SELECT b FROM Bill b JOIN FETCH b.unit u JOIN FETCH u.block JOIN FETCH b.quarter " +
           "WHERE b.quarter.id = :quarterId AND b.status = :status ORDER BY u.block.blockName, u.unitNumber")
    List<Bill> findByQuarterIdAndStatus(@Param("quarterId") Long quarterId,
                                        @Param("status") BillStatus status);

    @Query("SELECT b FROM Bill b JOIN FETCH b.unit u JOIN FETCH u.block JOIN FETCH b.quarter " +
           "WHERE b.unit.id = :unitId ORDER BY b.quarter.year DESC, b.quarter.quarterNumber DESC")
    List<Bill> findByUnitIdWithDetails(@Param("unitId") Long unitId);

    @Query("SELECT b FROM Bill b JOIN FETCH b.unit u JOIN FETCH u.block JOIN FETCH b.quarter " +
           "WHERE b.quarter.id = :quarterId AND u.block.id = :blockId ORDER BY u.unitNumber")
    List<Bill> findByQuarterIdAndBlockId(@Param("quarterId") Long quarterId,
                                         @Param("blockId") Long blockId);

    @Query("SELECT b FROM Bill b WHERE b.unit.id = :unitId AND b.status NOT IN ('PAID','OVERPAID','CANCELLED') " +
           "ORDER BY b.quarter.year ASC, b.quarter.quarterNumber ASC")
    List<Bill> findUnpaidBillsByUnitId(@Param("unitId") Long unitId);

    @Query("SELECT COUNT(b) FROM Bill b WHERE b.quarter.id = :quarterId AND b.status = :status")
    Long countByQuarterIdAndStatus(@Param("quarterId") Long quarterId, @Param("status") BillStatus status);

    @Query("SELECT COALESCE(SUM(b.totalAmount),0) FROM Bill b WHERE b.quarter.id = :quarterId")
    Double sumTotalAmountByQuarterId(@Param("quarterId") Long quarterId);

    @Query("SELECT COALESCE(SUM(b.amountPaid),0) FROM Bill b WHERE b.quarter.id = :quarterId")
    Double sumAmountPaidByQuarterId(@Param("quarterId") Long quarterId);
}