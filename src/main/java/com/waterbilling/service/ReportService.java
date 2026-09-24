package com.waterbilling.service;

import com.waterbilling.dto.BillDTO;
import com.waterbilling.dto.DashboardDTO;
import com.waterbilling.dto.ReportDTO;
import com.waterbilling.entity.Bill;
import com.waterbilling.entity.Quarter;
import com.waterbilling.enums.BillStatus;
import com.waterbilling.repository.BillRepository;
import com.waterbilling.repository.QuarterRepository;
import com.waterbilling.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final BillRepository billRepository;
    private final QuarterRepository quarterRepository;
    private final UnitRepository unitRepository;
    private final BillService billService;

    public ReportDTO getQuarterReport(Long quarterId) {
        Quarter quarter = quarterRepository.findById(quarterId)
                .orElseThrow(() -> new RuntimeException("Quarter not found"));

        List<Bill> bills = billRepository.findByQuarterIdWithDetails(quarterId);

        long paidCount = bills.stream().filter(b -> b.getStatus() == BillStatus.PAID || b.getStatus() == BillStatus.OVERPAID).count();
        long unpaidCount = bills.stream().filter(b -> b.getStatus() == BillStatus.GENERATED || b.getStatus() == BillStatus.UNPAID).count();
        long partialCount = bills.stream().filter(b -> b.getStatus() == BillStatus.PARTIALLY_PAID).count();

        BigDecimal totalBilled = bills.stream().map(Bill::getNetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCollected = bills.stream().map(Bill::getAmountPaid).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalOutstanding = bills.stream()
                .filter(b -> b.getBalanceAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(Bill::getBalanceAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<BillDTO> billDTOs = bills.stream().map(billService::toDTO).toList();

        return ReportDTO.builder()
                .quarterLabel(quarter.getLabel())
                .totalUnits((long) unitRepository.findByActiveTrue().size())
                .billsGenerated((long) bills.size())
                .paidCount(paidCount)
                .unpaidCount(unpaidCount)
                .partiallyPaidCount(partialCount)
                .totalBilled(totalBilled)
                .totalCollected(totalCollected)
                .totalOutstanding(totalOutstanding)
                .bills(billDTOs)
                .build();
    }

    public DashboardDTO getDashboard() {
        List<Quarter> quarters = quarterRepository.findAllByOrderByYearDescQuarterNumberDesc();
        long totalUnits = unitRepository.findByActiveTrue().size();

        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setTotalUnits(totalUnits);
        dashboard.setActiveQuarters((long) quarters.size());

        if (!quarters.isEmpty()) {
            Quarter latest = quarters.get(0);
            dashboard.setCurrentQuarter(latest.getLabel());

            List<Bill> currentBills = billRepository.findByQuarterId(latest.getId());
            dashboard.setCurrentQuarterBills((long) currentBills.size());

            long paid = currentBills.stream()
                    .filter(b -> b.getStatus() == BillStatus.PAID || b.getStatus() == BillStatus.OVERPAID).count();
            dashboard.setCurrentQuarterPaid(paid);
            dashboard.setCurrentQuarterUnpaid((long) currentBills.size() - paid);

            dashboard.setCurrentQuarterTotalBilled(
                    currentBills.stream().map(Bill::getNetAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            dashboard.setCurrentQuarterCollected(
                    currentBills.stream().map(Bill::getAmountPaid).reduce(BigDecimal.ZERO, BigDecimal::add));
            dashboard.setCurrentQuarterOutstanding(
                    dashboard.getCurrentQuarterTotalBilled().subtract(dashboard.getCurrentQuarterCollected()));
        }

        // Total outstanding across all quarters
        List<Bill> allBills = billRepository.findAll();
        dashboard.setTotalOutstandingAllTime(
                allBills.stream()
                        .filter(b -> b.getBalanceAmount() != null && b.getBalanceAmount().compareTo(BigDecimal.ZERO) > 0)
                        .map(Bill::getBalanceAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        return dashboard;
    }
}