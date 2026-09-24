package com.waterbilling.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.waterbilling.enums.BillStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bills", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"unit_id", "quarter_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String billNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quarter_id", nullable = false)
    private Quarter quarter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_reading_id", nullable = false)
    private MeterReading meterReading;

    @Column(nullable = false)
    private Double unitsConsumed;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal ratePerUnit;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal previousBalance; // carry forward from previous quarter

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal netAmount; // totalAmount + previousBalance

    @Column(precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(precision = 10, scale = 2)
    private BigDecimal balanceAmount; // netAmount - amountPaid

    @Enumerated(EnumType.STRING)
    private BillStatus status;

    private LocalDate billDate;

    private LocalDate dueDate;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = BillStatus.GENERATED;
        if (amountPaid == null) amountPaid = BigDecimal.ZERO;
        if (previousBalance == null) previousBalance = BigDecimal.ZERO;
        calculateNetAndBalance();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateNetAndBalance();
    }

    private void calculateNetAndBalance() {
        if (netAmount == null) {
            netAmount = totalAmount.add(previousBalance != null ? previousBalance : BigDecimal.ZERO);
        }
        if (balanceAmount == null) {
            balanceAmount = netAmount.subtract(amountPaid != null ? amountPaid : BigDecimal.ZERO);
        }
    }
}