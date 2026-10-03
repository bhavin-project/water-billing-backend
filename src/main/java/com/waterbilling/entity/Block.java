package com.waterbilling.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "blocks")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
@ToString(exclude = "units")
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String blockName;

    private Integer totalUnits;

    @JsonIgnore  // <--- ADD THIS ANNOTATION TO PREVENT SERIALIZATION ERROR
    @OneToMany(mappedBy = "block", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Unit> units;
}