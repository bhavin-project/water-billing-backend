package com.waterbilling.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "quarters", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"year", "quarterNumber"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Quarter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer quarterNumber; // 1,2,3,4

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String label; // e.g., "Q1-2024 (Jan-Mar)"

    private boolean closed = false;
}