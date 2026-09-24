package com.waterbilling.service;

import com.waterbilling.dto.BillDTO;
import com.waterbilling.dto.PaymentDTO;
import com.waterbilling.entity.*;
import com.waterbilling.enums.BillStatus;
import com.waterbilling.exception.ResourceNotFoundException;
import com.waterbilling.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final MeterReadingRepository meterReadingRepository;
    private final QuarterRepository quarterRepository;
    private final UnitRepository unitRepository;
    private final RateConfigService rateConfigService;

    @Transactional
    public List<Bill> generateBillsForQuarter(Long quarterId) {
        Quarter quarter = quarterRepository.findById(quarterId)
                .orElseThrow(() -> new ResourceNotFoundException("Quarter not found"));

        List<MeterReading> readings = meterReadingRepository.findByQuarterId(quarterId);
        if (readings.isEmpty()) {
            throw new IllegalArgumentException(
                    "No meter readings found for this quarter. Please enter readings first.");
        }

        BigDecimal ratePerUnit = rateConfigService.getRateForDate(quarter.getStartDate());
        List<Bill> bills = new ArrayList<>();
        int newBillCount = 0;

        for (MeterReading reading : readings) {
            // Skip if bill already generated for this unit+quarter
            Optional<Bill> existingBill = billRepository.findByUnitIdAndQuarterId(
                    reading.getUnit().getId(), quarterId);
            if (existingBill.isPresent()) {
                bills.add(existingBill.get());
                continue;
            }

            BigDecimal totalAmount = ratePerUnit.multiply(
                    BigDecimal.valueOf(reading.getUnitsConsumed()));

            // Calculate carry forward balance from previous unpaid bills
            BigDecimal previousBalance = calculatePreviousBalance(
                    reading.getUnit().getId(), quarterId);

            BigDecimal netAmount = totalAmount.add(previousBalance);

            Bill bill = new Bill();
            bill.setBillNumber(generateBillNumber(quarter, reading.getUnit()));
            bill.setUnit(reading.getUnit());
            bill.setQuarter(quarter);
            bill.setMeterReading(reading);
            bill.setUnitsConsumed(reading.getUnitsConsumed());
            bill.setRatePerUnit(ratePerUnit);
            bill.setTotalAmount(totalAmount);
            bill.setPreviousBalance(previousBalance);
            bill.setNetAmount(netAmount);
            bill.setAmountPaid(BigDecimal.ZERO);
            bill.setBalanceAmount(netAmount);
            bill.setStatus(BillStatus.GENERATED);
            bill.setBillDate(LocalDate.now());
            bill.setDueDate(LocalDate.now().plusDays(30));

            bills.add(billRepository.save(bill));

            // Lock the meter reading so it can't be modified
            reading.setLocked(true);
            meterReadingRepository.save(reading);

            newBillCount++;
        }

        return bills;
    }

    private BigDecimal calculatePreviousBalance(Long unitId, Long currentQuarterId) {
        List<Bill> unpaidBills = billRepository.findUnpaidBillsByUnitId(unitId);
        return unpaidBills.stream()
                .filter(b -> !b.getQuarter().getId().equals(currentQuarterId))
                .map(Bill::getBalanceAmount)
                .filter(bal -> bal.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateBillNumber(Quarter quarter, Unit unit) {
        return "BILL-" + quarter.getYear() + "Q" + quarter.getQuarterNumber()
                + "-" + unit.getUnitNumber();
    }

    public List<BillDTO> getBillsByQuarter(Long quarterId) {
        return billRepository.findByQuarterIdWithDetails(quarterId).stream()
                .map(this::toDTO).toList();
    }

    public List<BillDTO> getBillsByQuarterAndBlock(Long quarterId, Long blockId) {
        return billRepository.findByQuarterIdAndBlockId(quarterId, blockId).stream()
                .map(this::toDTO).toList();
    }

    public List<BillDTO> getBillsByQuarterAndStatus(Long quarterId, BillStatus status) {
        return billRepository.findByQuarterIdAndStatus(quarterId, status).stream()
                .map(this::toDTO).toList();
    }

    public List<BillDTO> getBillsByUnit(Long unitId) {
        return billRepository.findByUnitIdWithDetails(unitId).stream()
                .map(this::toDTO).toList();
    }

    public BillDTO getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        return toDTO(bill);
    }

    public BillDTO toDTO(Bill bill) {
        List<PaymentDTO> paymentDTOs = new ArrayList<>();
        if (bill.getPayments() != null) {
            paymentDTOs = bill.getPayments().stream().map(p -> PaymentDTO.builder()
                    .id(p.getId())
                    .receiptNumber(p.getReceiptNumber())
                    .billId(bill.getId())
                    .amountPaid(p.getAmountPaid())
                    .paymentMode(p.getPaymentMode())
                    .transactionNumber(p.getTransactionNumber())
                    .paymentDate(p.getPaymentDate())
                    .remarks(p.getRemarks())
                    .build()
            ).toList();
        }

        return BillDTO.builder()
                .id(bill.getId())
                .billNumber(bill.getBillNumber())
                .unitId(bill.getUnit().getId())
                .unitNumber(bill.getUnit().getUnitNumber())
                .blockName(bill.getUnit().getBlock().getBlockName())
                .ownerName(bill.getUnit().getOwnerName())
                .quarterId(bill.getQuarter().getId())
                .quarterLabel(bill.getQuarter().getLabel())
                .previousReading(bill.getMeterReading().getPreviousReading())
                .currentReading(bill.getMeterReading().getCurrentReading())
                .unitsConsumed(bill.getUnitsConsumed())
                .ratePerUnit(bill.getRatePerUnit())
                .totalAmount(bill.getTotalAmount())
                .previousBalance(bill.getPreviousBalance())
                .netAmount(bill.getNetAmount())
                .amountPaid(bill.getAmountPaid())
                .balanceAmount(bill.getBalanceAmount())
                .status(bill.getStatus())
                .billDate(bill.getBillDate())
                .dueDate(bill.getDueDate())
                .payments(paymentDTOs)
                .build();
    }
}