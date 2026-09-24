package com.waterbilling.controller;

import com.waterbilling.dto.PaymentDTO;
import com.waterbilling.dto.ReceiptDTO;
import com.waterbilling.entity.Payment;
import com.waterbilling.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> recordPayment(@RequestBody PaymentDTO dto) {
        Payment payment = paymentService.recordPayment(dto);
        return ResponseEntity.ok(Map.of(
                "message", "Payment recorded successfully",
                "receiptNumber", payment.getReceiptNumber(),
                "paymentId", payment.getId()
        ));
    }

    @GetMapping("/receipt/{paymentId}")
    public ResponseEntity<ReceiptDTO> getReceipt(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getReceipt(paymentId));
    }

    @GetMapping("/receipt/number/{receiptNumber}")
    public ResponseEntity<ReceiptDTO> getReceiptByNumber(@PathVariable String receiptNumber) {
        return ResponseEntity.ok(paymentService.getReceiptByNumber(receiptNumber));
    }

    @GetMapping("/quarter/{quarterId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByQuarter(@PathVariable Long quarterId) {
        return ResponseEntity.ok(paymentService.getPaymentsByQuarter(quarterId));
    }

    @GetMapping("/unit/{unitId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByUnit(@PathVariable Long unitId) {
        return ResponseEntity.ok(paymentService.getPaymentsByUnit(unitId));
    }

    @GetMapping("/bill/{billId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByBill(@PathVariable Long billId) {
        return ResponseEntity.ok(paymentService.getPaymentsByBill(billId));
    }
}