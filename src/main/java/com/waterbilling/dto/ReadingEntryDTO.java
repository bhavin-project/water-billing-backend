package com.waterbilling.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReadingEntryDTO {
    private Long unitId;
    private Long quarterId;
    private Double currentReading; // only this is manually entered
}