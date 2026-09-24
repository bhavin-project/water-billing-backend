package com.waterbilling.controller;

import com.waterbilling.dto.DashboardDTO;
import com.waterbilling.dto.ReportDTO;
import com.waterbilling.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard() {
        return ResponseEntity.ok(reportService.getDashboard());
    }

    @GetMapping("/quarter/{quarterId}")
    public ResponseEntity<ReportDTO> getQuarterReport(@PathVariable Long quarterId) {
        return ResponseEntity.ok(reportService.getQuarterReport(quarterId));
    }
}