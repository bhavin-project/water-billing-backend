package com.waterbilling.dto;

import com.waterbilling.enums.PaymentMode;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentDTO {
    private Long id;
    private String receiptNumber;
    private Long billId;
    private String billNumber;
    private Long unitId;
    private String unitNumber;
    private String blockName;
    private String ownerName;
    private String quarterLabel;
    private BigDecimal billAmount;
    private BigDecimal amountPaid;
    private PaymentMode paymentMode;
    private String transactionNumber;
    private LocalDate paymentDate;
    private String remarks;
    private BigDecimal balanceAfterPayment;
}