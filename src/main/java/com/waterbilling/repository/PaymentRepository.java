package com.waterbilling.repository;

import com.waterbilling.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByReceiptNumber(String receiptNumber);

    List<Payment> findByBillId(Long billId);

    @Query("SELECT p FROM Payment p JOIN FETCH p.bill b JOIN FETCH b.unit u JOIN FETCH u.block " +
           "JOIN FETCH b.quarter WHERE b.quarter.id = :quarterId " +
           "ORDER BY p.paymentDate DESC")
    List<Payment> findByQuarterIdWithDetails(@Param("quarterId") Long quarterId);

    @Query("SELECT p FROM Payment p JOIN FETCH p.bill b JOIN FETCH b.unit u JOIN FETCH u.block " +
           "JOIN FETCH b.quarter WHERE b.unit.id = :unitId " +
           "ORDER BY p.paymentDate DESC")
    List<Payment> findByUnitIdWithDetails(@Param("unitId") Long unitId);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(p.receiptNumber, 5) AS integer)),0) FROM Payment p " +
           "WHERE p.receiptNumber LIKE :prefix%")
    Integer findMaxReceiptNumber(@Param("prefix") String prefix);
}