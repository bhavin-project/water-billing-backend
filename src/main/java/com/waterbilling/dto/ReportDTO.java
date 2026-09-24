package com.waterbilling.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportDTO {
    private String quarterLabel;
    private Long totalUnits;
    private Long billsGenerated;
    private Long paidCount;
    private Long unpaidCount;
    private Long partiallyPaidCount;
    private BigDecimal totalBilled;
    private BigDecimal totalCollected;
    private BigDecimal totalOutstanding;
    private List<BillDTO> bills;
}