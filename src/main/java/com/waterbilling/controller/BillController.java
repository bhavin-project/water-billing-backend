package com.waterbilling.controller;

import com.waterbilling.dto.BillDTO;
import com.waterbilling.enums.BillStatus;
import com.waterbilling.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping("/generate/{quarterId}")
    public ResponseEntity<Map<String, Object>> generateBills(@PathVariable Long quarterId) {
        var bills = billService.generateBillsForQuarter(quarterId);
        return ResponseEntity.ok(Map.of(
                "message", "Bills generated successfully",
                "count", bills.size()
        ));
    }

    @GetMapping("/quarter/{quarterId}")
    public ResponseEntity<List<BillDTO>> getBillsByQuarter(@PathVariable Long quarterId) {
        return ResponseEntity.ok(billService.getBillsByQuarter(quarterId));
    }

    @GetMapping("/quarter/{quarterId}/block/{blockId}")
    public ResponseEntity<List<BillDTO>> getBillsByQuarterAndBlock(
            @PathVariable Long quarterId, @PathVariable Long blockId) {
        return ResponseEntity.ok(billService.getBillsByQuarterAndBlock(quarterId, blockId));
    }

    @GetMapping("/quarter/{quarterId}/status/{status}")
    public ResponseEntity<List<BillDTO>> getBillsByQuarterAndStatus(
            @PathVariable Long quarterId, @PathVariable BillStatus status) {
        return ResponseEntity.ok(billService.getBillsByQuarterAndStatus(quarterId, status));
    }

    @GetMapping("/unit/{unitId}")
    public ResponseEntity<List<BillDTO>> getBillsByUnit(@PathVariable Long unitId) {
        return ResponseEntity.ok(billService.getBillsByUnit(unitId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillDTO> getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(billService.getBillById(id));
    }
}