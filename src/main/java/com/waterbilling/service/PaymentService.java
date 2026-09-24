package com.waterbilling.service;

import com.waterbilling.dto.PaymentDTO;
import com.waterbilling.dto.ReceiptDTO;
import com.waterbilling.entity.Bill;
import com.waterbilling.entity.Payment;
import com.waterbilling.enums.BillStatus;
import com.waterbilling.exception.ResourceNotFoundException;
import com.waterbilling.repository.BillRepository;
import com.waterbilling.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;

    @Transactional
    public Payment recordPayment(PaymentDTO dto) {
        Bill bill = billRepository.findById(dto.getBillId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + dto.getBillId()));

        Payment payment = new Payment();
        payment.setReceiptNumber(generateReceiptNumber());
        payment.setBill(bill);
        payment.setAmountPaid(dto.getAmountPaid());
        payment.setPaymentMode(dto.getPaymentMode());
        payment.setTransactionNumber(dto.getTransactionNumber());
        payment.setPaymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : LocalDate.now());
        payment.setRemarks(dto.getRemarks());

        payment = paymentRepository.save(payment);

        // Update bill
        BigDecimal totalPaid = bill.getAmountPaid().add(dto.getAmountPaid());
        bill.setAmountPaid(totalPaid);
        bill.setBalanceAmount(bill.getNetAmount().subtract(totalPaid));

        int comparison = totalPaid.compareTo(bill.getNetAmount());
        if (comparison >= 0) {
            bill.setStatus(comparison > 0 ? BillStatus.OVERPAID : BillStatus.PAID);
        } else {
            bill.setStatus(BillStatus.PARTIALLY_PAID);
        }

        billRepository.save(bill);
        return payment;
    }

    private String generateReceiptNumber() {
        String prefix = "RCP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        Integer maxNum = paymentRepository.findMaxReceiptNumber(prefix);
        int nextNum = (maxNum != null ? maxNum : 0) + 1;
        return prefix + String.format("%04d", nextNum);
    }

    public ReceiptDTO getReceipt(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return buildReceipt(payment);
    }

    public ReceiptDTO getReceiptByNumber(String receiptNumber) {
        Payment payment = paymentRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found: " + receiptNumber));
        return buildReceipt(payment);
    }

    private ReceiptDTO buildReceipt(Payment payment) {
        Bill bill = payment.getBill();
        return ReceiptDTO.builder()
                .receiptNumber(payment.getReceiptNumber())
                .billNumber(bill.getBillNumber())
                .unitNumber(bill.getUnit().getUnitNumber())
                .blockName(bill.getUnit().getBlock().getBlockName())
                .ownerName(bill.getUnit().getOwnerName())
                .quarterLabel(bill.getQuarter().getLabel())
                .previousReading(bill.getMeterReading().getPreviousReading())
                .currentReading(bill.getMeterReading().getCurrentReading())
                .unitsConsumed(bill.getUnitsConsumed())
                .ratePerUnit(bill.getRatePerUnit())
                .totalBillAmount(bill.getTotalAmount())
                .previousBalance(bill.getPreviousBalance())
                .netAmount(bill.getNetAmount())
                .amountPaid(payment.getAmountPaid())
                .totalPaidSoFar(bill.getAmountPaid())
                .balanceAmount(bill.getBalanceAmount())
                .paymentMode(payment.getPaymentMode())
                .transactionNumber(payment.getTransactionNumber())
                .paymentDate(payment.getPaymentDate())
                .societyName("Your Society Name - 94 Units")
                .build();
    }

    public List<PaymentDTO> getPaymentsByQuarter(Long quarterId) {
        return paymentRepository.findByQuarterIdWithDetails(quarterId).stream()
                .map(this::toDTO).toList();
    }

    public List<PaymentDTO> getPaymentsByUnit(Long unitId) {
        return paymentRepository.findByUnitIdWithDetails(unitId).stream()
                .map(this::toDTO).toList();
    }

    public List<PaymentDTO> getPaymentsByBill(Long billId) {
        return paymentRepository.findByBillId(billId).stream()
                .map(this::toDTO).toList();
    }

    private PaymentDTO toDTO(Payment p) {
        Bill bill = p.getBill();
        return PaymentDTO.builder()
                .id(p.getId())
                .receiptNumber(p.getReceiptNumber())
                .billId(bill.getId())
                .billNumber(bill.getBillNumber())
                .unitId(bill.getUnit().getId())
                .unitNumber(bill.getUnit().getUnitNumber())
                .blockName(bill.getUnit().getBlock().getBlockName())
                .ownerName(bill.getUnit().getOwnerName())
                .quarterLabel(bill.getQuarter().getLabel())
                .billAmount(bill.getNetAmount())
                .amountPaid(p.getAmountPaid())
                .paymentMode(p.getPaymentMode())
                .transactionNumber(p.getTransactionNumber())
                .paymentDate(p.getPaymentDate())
                .remarks(p.getRemarks())
                .balanceAfterPayment(bill.getBalanceAmount())
                .build();
    }
}