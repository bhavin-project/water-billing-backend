package com.waterbilling.dto;

import com.waterbilling.enums.PaymentMode;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReceiptDTO {
    private String receiptNumber;
    private String billNumber;
    private String unitNumber;
    private String blockName;
    private String ownerName;
    private String quarterLabel;
    private Double previousReading;
    private Double currentReading;
    private Double unitsConsumed;
    private BigDecimal ratePerUnit;
    private BigDecimal totalBillAmount;
    private BigDecimal previousBalance;
    private BigDecimal netAmount;
    private BigDecimal amountPaid;
    private BigDecimal totalPaidSoFar;
    private BigDecimal balanceAmount;
    private PaymentMode paymentMode;
    private String transactionNumber;
    private LocalDate paymentDate;
    private String societyName;
}