package com.waterbilling.dto;

import com.waterbilling.enums.BillStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BillDTO {
    private Long id;
    private String billNumber;
    private Long unitId;
    private String unitNumber;
    private String blockName;
    private String ownerName;
    private Long quarterId;
    private String quarterLabel;
    private Double previousReading;
    private Double currentReading;
    private Double unitsConsumed;
    private BigDecimal ratePerUnit;
    private BigDecimal totalAmount;
    private BigDecimal previousBalance;
    private BigDecimal netAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceAmount;
    private BillStatus status;
    private LocalDate billDate;
    private LocalDate dueDate;
    private List<PaymentDTO> payments;
}