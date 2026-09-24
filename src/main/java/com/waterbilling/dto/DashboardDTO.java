package com.waterbilling.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardDTO {
    private Long totalUnits;
    private Long activeQuarters;
    private String currentQuarter;
    private Long currentQuarterBills;
    private Long currentQuarterPaid;
    private Long currentQuarterUnpaid;
    private BigDecimal currentQuarterTotalBilled;
    private BigDecimal currentQuarterCollected;
    private BigDecimal currentQuarterOutstanding;
    private BigDecimal totalOutstandingAllTime;
}