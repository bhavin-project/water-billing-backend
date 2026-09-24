package com.waterbilling.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BulkMeterReadingDTO {
    private Long quarterId;
    private List<MeterReadingDTO> readings;
}