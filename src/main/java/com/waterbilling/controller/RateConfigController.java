package com.waterbilling.controller;

import com.waterbilling.entity.RateConfig;
import com.waterbilling.service.RateConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rates")
@RequiredArgsConstructor
public class RateConfigController {

    private final RateConfigService rateConfigService;

    @GetMapping
    public ResponseEntity<List<RateConfig>> getAllRates() {
        return ResponseEntity.ok(rateConfigService.getAllRates());
    }

    @GetMapping("/active")
    public ResponseEntity<RateConfig> getActiveRate() {
        return ResponseEntity.ok(rateConfigService.getActiveRate());
    }

    @PostMapping
    public ResponseEntity<RateConfig> addRate(@RequestBody RateConfig rateConfig) {
        return ResponseEntity.ok(rateConfigService.addRate(rateConfig));
    }
}