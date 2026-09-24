package com.waterbilling.controller;

import com.waterbilling.entity.Unit;
import com.waterbilling.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUnits() {
        List<Unit> units = unitService.getAllActiveUnits();
        List<Map<String, Object>> result = units.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("unitNumber", u.getUnitNumber());
            map.put("blockId", u.getBlock().getId());
            map.put("blockName", u.getBlock().getBlockName());
            map.put("ownerName", u.getOwnerName());
            map.put("contactNumber", u.getContactNumber());
            map.put("email", u.getEmail());
            map.put("active", u.isActive());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/block/{blockId}")
    public ResponseEntity<List<Map<String, Object>>> getUnitsByBlock(@PathVariable Long blockId) {
        List<Unit> units = unitService.getUnitsByBlock(blockId);
        List<Map<String, Object>> result = units.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("unitNumber", u.getUnitNumber());
            map.put("blockId", u.getBlock().getId());
            map.put("blockName", u.getBlock().getBlockName());
            map.put("ownerName", u.getOwnerName());
            map.put("contactNumber", u.getContactNumber());
            map.put("email", u.getEmail());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Unit> updateUnit(@PathVariable Long id, @RequestBody Unit unit) {
        return ResponseEntity.ok(unitService.updateUnit(id, unit));
    }
}