package com.ridebooking.Notification_Service.Service;

import com.ridebooking.Notification_Service.Entity.Invoice;
import com.ridebooking.Notification_Service.Repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PDFgenerationService pdfGenerationService;

    @Transactional
    public Invoice getOrCreateInvoice(
            String email,
            String rideId,
            String paymentId,
            BigDecimal amount
    ) {

        return invoiceRepository.findByPaymentId(paymentId)
                .orElseGet(() -> {

                    String invoiceNumber =
                            "INV-" + System.currentTimeMillis();

                    byte[] pdf = pdfGenerationService.generateInvoice(
                            invoiceNumber,
                            rideId,
                            paymentId,
                            amount
                    );

                    Invoice invoice = Invoice.builder()
                            .invoiceNumber(invoiceNumber)
                            .rideId(rideId)
                            .paymentId(paymentId)
                            .userEmail(email)
                            .amount(amount)
                            .generatedAt(LocalDateTime.now())
                            .pdf(pdf)
                            .emailStatus("PENDING")
                            .build();

                    return invoiceRepository.saveAndFlush(invoice);
                });
    }

    @Transactional
    public void markEmailSent(String paymentId) {

        Invoice invoice = invoiceRepository.findByPaymentId(paymentId)
                .orElseThrow(() ->
                        new RuntimeException("Invoice not found"));

        invoice.setEmailStatus("SENT");
        invoice.setEmailSentAt(LocalDateTime.now());

        invoiceRepository.save(invoice);
    }

    @Transactional
    public void markEmailFailed(String paymentId) {

        Invoice invoice = invoiceRepository.findByPaymentId(paymentId)
                .orElseThrow(() ->
                        new RuntimeException("Invoice not found"));

        invoice.setEmailStatus("FAILED");

        invoiceRepository.save(invoice);
    }
}