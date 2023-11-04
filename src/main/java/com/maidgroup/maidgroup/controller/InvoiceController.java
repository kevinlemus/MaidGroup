package com.maidgroup.maidgroup.controller;

import com.maidgroup.maidgroup.dao.InvoiceRepository;
import com.maidgroup.maidgroup.model.Invoice;
import com.maidgroup.maidgroup.model.invoiceinfo.PaymentStatus;
import com.maidgroup.maidgroup.service.EmailService;
import com.maidgroup.maidgroup.service.InvoiceService;
import com.maidgroup.maidgroup.service.exceptions.InvalidInvoiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/invoices")
@CrossOrigin
public class InvoiceController {

    private InvoiceService invoiceService;
    private InvoiceRepository invoiceRepository;
    private EmailService emailService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService, InvoiceRepository invoiceRepository, EmailService emailService) {
        this.invoiceService = invoiceService;
        this.invoiceRepository = invoiceRepository;
        this.emailService = emailService;
    }

    @PostMapping("/create")
    public String createInvoice(@RequestBody Invoice invoice) {

            // validate invoice fields
            invoiceService.validateInvoice(invoice);

            // set invoice status to UNPAID
            invoice.setStatus((PaymentStatus.UNPAID));

            // generate payment link and send it to user
            String paymentLink = invoiceService.create(invoice);

            return "The unique link for this payment has been sent!";

    }

    @PostMapping("/webhook")
    public void handleWebhook(@RequestBody Map<String, Object> payload) {
        // Extract relevant information from webhook payload
        String type = (String) payload.get("type");
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        Map<String, Object> object = (Map<String, Object>) data.get("object");
        String orderId = (String) object.get("order_id");

        // Check if payment was updated
        if ("payment.updated".equals(type)) {
            // Extract payment status from payload
            String paymentStatus = (String) object.get("status");

            // Look up invoice by order ID
            Invoice invoice = invoiceRepository.findByOrderId(orderId);

            // Check if payment was successful
            if ("COMPLETED".equals(paymentStatus)) {
                // Update invoice status and send invoice to user
                if (invoice != null) {
                    invoice.setStatus(PaymentStatus.PAID);
                    invoiceService.completePayment(invoice);
                }
            } else if ("FAILED".equals(paymentStatus)) {
                // Update invoice status
                if (invoice != null) {
                    invoice.setStatus(PaymentStatus.FAILED);
                    invoiceRepository.save(invoice);

                    // Generate new payment link and send it to user
                    String newPaymentLink = invoiceService.create(invoice);
                    String subject = "Your New Invoice Payment Link";
                    String body = "Your previous payment attempt failed. Please try again with your new payment link: " + newPaymentLink;
                    emailService.sendEmail(invoice.getClientEmail(), subject, body);
                }
            }
        }
    }




    //Using frontend to handle redirect page after successful/failed payment.
    //Square will redirect the user back to website after completed payment process with information
    //about the payment in the URL. This will be used to determine redirect page.

}

