package com.maidgroup.maidgroup.controller;

import com.maidgroup.maidgroup.dao.InvoiceRepository;
import com.maidgroup.maidgroup.model.Invoice;
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

    @Autowired
    public InvoiceController(InvoiceService invoiceService, InvoiceRepository invoiceRepository) {
        this.invoiceService = invoiceService;
        this.invoiceRepository = invoiceRepository;
    }

    @PostMapping
    public String createInvoice(@RequestBody Invoice invoice) {

            // validate invoice fields
            invoiceService.validateInvoice(invoice);

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

        // Check if payment was completed
        if ("payment.updated".equals(type)) {
            // Look up invoice by order ID
            Invoice invoice = invoiceRepository.findByOrderId(orderId);

            // Update invoice status and send invoice to user
            if (invoice != null) {
                invoiceService.completePayment(invoice);
            }
        }
    }
}

