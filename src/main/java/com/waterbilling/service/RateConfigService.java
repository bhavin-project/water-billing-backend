package com.waterbilling.service;

import com.waterbilling.entity.RateConfig;
import com.waterbilling.repository.RateConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RateConfigService {
    private final RateConfigRepository rateConfigRepository;

    public List<RateConfig> getAllRates() {
        return rateConfigRepository.findAllByOrderByEffectiveFromDesc();
    }

    public RateConfig getActiveRate() {
        return rateConfigRepository.findByActiveTrue()
                .orElseThrow(() -> new RuntimeException("No active rate configuration found"));
    }

    public BigDecimal getRateForDate(LocalDate date) {
        List<RateConfig> rates = rateConfigRepository
                .findByEffectiveFromLessThanEqualAndActiveTrueOrderByEffectiveFromDesc(date);
        if (!rates.isEmpty()) {
            return rates.get(0).getRatePerUnit();
        }
        return getActiveRate().getRatePerUnit();
    }

    @Transactional
    public RateConfig addRate(RateConfig rateConfig) {
        // Deactivate current active rate
        rateConfigRepository.findByActiveTrue().ifPresent(existing -> {
            existing.setActive(false);
            existing.setEffectiveTo(rateConfig.getEffectiveFrom().minusDays(1));
            rateConfigRepository.save(existing);
        });
        rateConfig.setActive(true);
        return rateConfigRepository.save(rateConfig);
    }
}