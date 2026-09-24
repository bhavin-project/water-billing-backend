package com.waterbilling.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MeterReadingDTO {
    private Long id;
    private Long unitId;
    private String unitNumber;
    private String blockName;
    private String ownerName;
    private Long quarterId;
    private String quarterLabel;
    private Double previousReading;    // auto-fetched
    private Double currentReading;     // manually entered
    private Double unitsConsumed;      // auto-calculated
    private BigDecimal ratePerUnit;    // auto-fetched
    private BigDecimal estimatedAmount;// auto-calculated
    private LocalDate readingDate;
    private boolean locked;
    private boolean hasReading;        // whether reading exists for this quarter
}