package com.waterbilling.controller;

import com.waterbilling.dto.*;
import com.waterbilling.entity.Quarter;
import com.waterbilling.service.MeterReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/meter-readings")
@RequiredArgsConstructor
public class MeterReadingController {

    private final MeterReadingService meterReadingService;

    // ===== Quarter endpoints =====

    @GetMapping("/quarters")
    public ResponseEntity<List<Quarter>> getAllQuarters() {
        return ResponseEntity.ok(meterReadingService.getAllQuarters());
    }

    @PostMapping("/quarters")
    public ResponseEntity<Quarter> createQuarter(@RequestBody Map<String, Integer> request) {
        Quarter quarter = meterReadingService.getOrCreateQuarter(
                request.get("year"), request.get("quarterNumber"));
        return ResponseEntity.ok(quarter);
    }

    // ===== Get form data with auto-populated previous readings =====

    @GetMapping("/form-data/{quarterId}")
    public ResponseEntity<ReadingFormDataDTO> getReadingFormData(
            @PathVariable Long quarterId,
            @RequestParam(required = false) Long blockId) {
        return ResponseEntity.ok(meterReadingService.getReadingFormData(quarterId, blockId));
    }

    // ===== Save single reading (only current reading entered manually) =====

    @PostMapping("/save")
    public ResponseEntity<MeterReadingDTO> saveSingleReading(@RequestBody ReadingEntryDTO dto) {
        return ResponseEntity.ok(meterReadingService.saveSingleReading(dto));
    }

    // ===== Save all readings at once =====

    @PostMapping("/save-all")
    public ResponseEntity<Map<String, Object>> saveBulkReadings(@RequestBody BulkReadingEntryDTO dto) {
        List<MeterReadingDTO> saved = meterReadingService.saveBulkReadings(dto);
        return ResponseEntity.ok(Map.of(
                "message", saved.size() + " readings saved successfully",
                "count", saved.size(),
                "readings", saved
        ));
    }

    // ===== View readings =====

    @GetMapping("/quarter/{quarterId}")
    public ResponseEntity<List<MeterReadingDTO>> getReadingsByQuarter(@PathVariable Long quarterId) {
        return ResponseEntity.ok(meterReadingService.getReadingsByQuarter(quarterId));
    }

    @GetMapping("/quarter/{quarterId}/block/{blockId}")
    public ResponseEntity<List<MeterReadingDTO>> getReadingsByQuarterAndBlock(
            @PathVariable Long quarterId, @PathVariable Long blockId) {
        return ResponseEntity.ok(meterReadingService.getReadingsByQuarterAndBlock(quarterId, blockId));
    }

    @GetMapping("/unit/{unitId}")
    public ResponseEntity<List<MeterReadingDTO>> getReadingsByUnit(@PathVariable Long unitId) {
        return ResponseEntity.ok(meterReadingService.getReadingsByUnit(unitId));
    }

    @GetMapping("/unit/{unitId}/last-reading")
    public ResponseEntity<Double> getLastReading(@PathVariable Long unitId) {
        return ResponseEntity.ok(meterReadingService.getLastCurrentReading(unitId));
    }
}