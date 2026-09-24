package com.waterbilling.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReadingFormDataDTO {
    private Long quarterId;
    private String quarterLabel;
    private BigDecimal currentRate;
    private List<MeterReadingDTO> readings;
    private int totalUnits;
    private int readingsEntered;
    private int readingsPending;
}