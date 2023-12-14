package com.maidgroup.maidgroup.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maidgroup.maidgroup.dao.InvoiceRepository;
import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.Invoice;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.invoiceinfo.PaymentStatus;
import com.maidgroup.maidgroup.service.EmailService;
import com.maidgroup.maidgroup.service.InvoiceService;
import com.maidgroup.maidgroup.service.UserService;
import com.maidgroup.maidgroup.service.exceptions.InvalidInvoiceException;
import com.maidgroup.maidgroup.service.impl.InvoiceServiceImpl;
import com.maidgroup.maidgroup.util.square.WebhookSignatureVerifier;
import com.squareup.square.exceptions.ApiException;
import com.squareup.square.models.BatchRetrieveOrdersRequest;
import com.squareup.square.models.BatchRetrieveOrdersResponse;
import com.squareup.square.models.Order;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/invoices")
@CrossOrigin
public class InvoiceController {

    private InvoiceServiceImpl invoiceServiceImpl;
    private InvoiceService invoiceService;
    private InvoiceRepository invoiceRepository;
    private UserService userService;
    private UserRepository userRepository;
    private EmailService emailService;
    private WebhookSignatureVerifier webhookSignatureVerifier;
    @Value("${SQUARE_SIGNATURE_KEY}")
    private String signatureKey;
    @Value("${square.location-id}")
    private String squareLocationId;

    @Autowired
    public InvoiceController(InvoiceServiceImpl invoiceServiceImpl, InvoiceService invoiceService, InvoiceRepository invoiceRepository, UserRepository userRepository, UserService userService, EmailService emailService, WebhookSignatureVerifier webhookSignatureVerifier) {
        this.invoiceServiceImpl = invoiceServiceImpl;
        this.invoiceService = invoiceService;
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
        this.userService = userService;
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
            invoiceService.create(invoice, idempotencyKey);

            return "The unique link for this payment has been sent!";

    }

    @PostMapping("/webhook")
    public void handleWebhook(@RequestHeader("X-Square-Signature") String signature, @RequestBody String payload) throws IOException, NoSuchAlgorithmException, InvalidKeyException, ApiException {
        log.info("Received webhook from Square");
        // Verify the signature
        if (!webhookSignatureVerifier.verifySignature(payload, signature)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid signature");
        }

        //Parse the payload into a Map
        Map<String, Object> payloadMap = new ObjectMapper().readValue(payload, new TypeReference<Map<String, Object>>() {});

        log.info("Payload: {}", payload);
        // Extract relevant information from webhook payload
        String type = (String) payloadMap.get("type");
// Extract relevant information from webhook payload
        Map<String, Object> data = (Map<String, Object>) payloadMap.get("data");
        Map<String, Object> object = (Map<String, Object>) data.get("object");
        Map<String, Object> payment = (Map<String, Object>) object.get("payment");

        // Extract the order ID from the payload
        String orderId = (String) payment.get("order_id");

        // Call the BatchRetrieveOrders endpoint to retrieve the order
        BatchRetrieveOrdersRequest request = new BatchRetrieveOrdersRequest.Builder(Collections.singletonList(orderId))
                .build();
        BatchRetrieveOrdersResponse response = invoiceServiceImpl.getSquareClient().getOrdersApi().batchRetrieveOrders(request);

        // Extract the referenceId from the order
        Order order = response.getOrders().get(0);
        String referenceId = order.getReferenceId();

        // Look up invoice by order ID
        Invoice invoice = invoiceRepository.findByOrderId(referenceId);

        log.info("Webhook reference ID and original order ID: " + referenceId);
        log.info("Type: {}", type);

        // Check if payment was updated
        if ("payment.updated".equals(type)) {
            // Extract payment status from payload
            String paymentStatus = (String) payment.get("status");

            log.info("Payment Status: {}", paymentStatus);

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

    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") Long id, Principal principal){
        User authUser = userRepository.findByUsername(principal.getName());
        invoiceService.delete(id, authUser);
        return "This invoice has been deleted.";

    }

}

    //Using frontend to handle redirect page after successful/failed payment.
    //Square will redirect the user back to website after completed payment process with information
    //about the payment in the URL. This will be used to determine redirect page.
