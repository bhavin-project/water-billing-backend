package com.waterbilling.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BulkReadingEntryDTO {
    private Long quarterId;
    private List<ReadingEntryDTO> readings;
}