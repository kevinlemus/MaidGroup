package com.maidgroup.maidgroup.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maidgroup.maidgroup.dao.InvoiceRepository;
import com.maidgroup.maidgroup.model.Invoice;
import com.maidgroup.maidgroup.model.invoiceinfo.PaymentStatus;
import com.maidgroup.maidgroup.service.EmailService;
import com.maidgroup.maidgroup.service.InvoiceService;
import com.maidgroup.maidgroup.service.exceptions.InvalidInvoiceException;
import com.maidgroup.maidgroup.util.square.WebhookSignatureVerifier;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/invoices")
@CrossOrigin
public class InvoiceController {

    private InvoiceService invoiceService;
    private InvoiceRepository invoiceRepository;
    private EmailService emailService;
    private WebhookSignatureVerifier webhookSignatureVerifier;
    @Value("${SQUARE_SIGNATURE_KEY}")
    private String signatureKey;

    @Autowired
    public InvoiceController(InvoiceService invoiceService, InvoiceRepository invoiceRepository, EmailService emailService, WebhookSignatureVerifier webhookSignatureVerifier) {
        this.invoiceService = invoiceService;
        this.invoiceRepository = invoiceRepository;
        this.emailService = emailService;
        this.webhookSignatureVerifier = webhookSignatureVerifier;
    }

    @PostMapping("/create")
    public String createInvoice(@RequestBody Invoice invoice) {
            // validate invoice fields
            invoiceService.validateInvoice(invoice);

            // generate a unique idempotency key
            String idempotencyKey = UUID.randomUUID().toString();

            // generate payment link and send it to user
            String paymentLink = invoiceService.create(invoice, idempotencyKey);

            return "The unique link for this payment has been sent!";

    }

    @PostMapping("/webhook")
    public void handleWebhook(@RequestHeader("X-Square-Signature") String signature, @RequestBody String payload) throws JsonProcessingException, NoSuchAlgorithmException, InvalidKeyException {
        log.info("Received webhook from Square");
        // Verify the signature
        if (!webhookSignatureVerifier.verifySignature(payload, signature, signatureKey)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid signature");
        }

        //Parse the payload into a Map
        Map<String, Object> payloadMap = new ObjectMapper().readValue(payload, new TypeReference<Map<String, Object>>() {});

        log.info("Payload: {}", payload);
        // Extract relevant information from webhook payload
        String type = (String) payloadMap.get("type");
        Map<String, Object> data = (Map<String, Object>) payloadMap.get("data");
        Map<String, Object> object = (Map<String, Object>) data.get("object");
        String orderId = (String) object.get("order_id");

        log.info("Type: {}", type);
        log.info("Order ID: {}", orderId);

        // Check if payment was updated
        if ("payment.updated".equals(type)) {
            // Extract payment status from payload
            Map<String, Object> payment = (Map<String, Object>) object.get("payment");
            String paymentStatus = (String) payment.get("status");

            log.info("Payment Status: {}", paymentStatus);
            // Look up invoice by order ID
            Invoice invoice = invoiceRepository.findByOrderId(orderId);
            if (invoice == null) {
                log.error("Invoice not found in database for order ID: " + orderId);
            } else {
                log.info("Invoice found in database: " + invoice);
            }
            log.info("Invoice found in database: " + invoice);

            log.info("Retrieved Invoice: {}", invoice);

            // Complete payment and update invoice status
            if (invoice != null) {
                invoiceService.completePayment(invoice, paymentStatus);
            }
        }
    }

}




    //Using frontend to handle redirect page after successful/failed payment.
    //Square will redirect the user back to website after completed payment process with information
    //about the payment in the URL. This will be used to determine redirect page.



