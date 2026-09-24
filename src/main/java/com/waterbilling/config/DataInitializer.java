package com.waterbilling.config;

import com.waterbilling.entity.Block;
import com.waterbilling.entity.RateConfig;
import com.waterbilling.entity.Unit;
import com.waterbilling.repository.BlockRepository;
import com.waterbilling.repository.RateConfigRepository;
import com.waterbilling.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final BlockRepository blockRepository;
    private final UnitRepository unitRepository;
    private final RateConfigRepository rateConfigRepository;

    @Override
    public void run(String... args) {
        if (blockRepository.count() == 0) {

            // ==================== BLOCK A ====================
            // 8 Floors × 8 Flats = 64 Units
            // Floor 1: A-101 to A-108
            // Floor 2: A-201 to A-208
            // ...
            // Floor 8: A-801 to A-808
            Block blockA = new Block();
            blockA.setBlockName("A");
            blockA.setTotalUnits(64);
            blockA = blockRepository.save(blockA);

            for (int floor = 1; floor <= 8; floor++) {
                for (int flat = 1; flat <= 8; flat++) {
                    Unit unit = new Unit();
                    // e.g., floor=1, flat=3 → "A-103"
                    //        floor=8, flat=5 → "A-805"
                    String unitNumber = "A-" + floor + "0" + flat;
                    unit.setUnitNumber(unitNumber);
                    unit.setBlock(blockA);
                    unit.setOwnerName("");
                    unit.setContactNumber("");
                    unit.setEmail("");
                    unit.setActive(true);
                    unitRepository.save(unit);
                }
            }

            // ==================== BLOCK B ====================
            // Floor 1-7: 4 Flats each = 28 Units
            // Floor 8:   2 Flats only =  2 Units
            // Total = 30 Units
            // Floor 1: B-101 to B-104
            // Floor 2: B-201 to B-204
            // ...
            // Floor 7: B-701 to B-704
            // Floor 8: B-801 to B-802
            Block blockB = new Block();
            blockB.setBlockName("B");
            blockB.setTotalUnits(30);
            blockB = blockRepository.save(blockB);

            for (int floor = 1; floor <= 8; floor++) {
                // Floor 8 has only 2 flats, rest have 4
                int flatsOnFloor = (floor == 8) ? 2 : 4;

                for (int flat = 1; flat <= flatsOnFloor; flat++) {
                    Unit unit = new Unit();
                    // e.g., floor=1, flat=2 → "B-102"
                    //        floor=8, flat=1 → "B-801"
                    String unitNumber = "B-" + floor + "0" + flat;
                    unit.setUnitNumber(unitNumber);
                    unit.setBlock(blockB);
                    unit.setOwnerName("");
                    unit.setContactNumber("");
                    unit.setEmail("");
                    unit.setActive(true);
                    unitRepository.save(unit);
                }
            }

            System.out.println("✅ Created 64 units for Block A (A-101 to A-808)");
            System.out.println("✅ Created 30 units for Block B (B-101 to B-802)");
        }

        // Default rate config
        if (rateConfigRepository.count() == 0) {
            RateConfig config = new RateConfig();
            config.setRatePerUnit(new BigDecimal("5.00"));
            config.setEffectiveFrom(LocalDate.of(2024, 1, 1));
            config.setEffectiveTo(null);
            config.setActive(true);
            config.setDescription("Current rate - Rs 5 per unit");
            rateConfigRepository.save(config);
        }
    }
}