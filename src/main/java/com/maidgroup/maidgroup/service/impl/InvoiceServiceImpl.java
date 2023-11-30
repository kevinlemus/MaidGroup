package com.maidgroup.maidgroup.service.impl;

import com.maidgroup.maidgroup.dao.InvoiceRepository;
import com.maidgroup.maidgroup.model.Invoice;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.invoiceinfo.PaymentStatus;
import com.maidgroup.maidgroup.service.EmailService;
import com.maidgroup.maidgroup.service.InvoiceService;
import com.maidgroup.maidgroup.service.exceptions.InvalidInvoiceException;
import com.maidgroup.maidgroup.util.payment.PaymentLinkGenerator;
import com.squareup.square.Environment;
import com.squareup.square.SquareClient;
import com.squareup.square.models.*;

import com.squareup.square.exceptions.ApiException;
import com.squareup.square.models.Error;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@Service
public class InvoiceServiceImpl implements InvoiceService {

    InvoiceRepository invoiceRepository;
    EmailService emailService;
    private PaymentLinkGenerator paymentLinkGenerator;
    private SquareClient squareClient;
    @Value("${square.location-id}")
    private String squareLocationId;
    @Value("${square.access-token}")
    private String squareAccessToken;

    @Autowired
    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, EmailService emailService, PaymentLinkGenerator paymentLinkGenerator) {
        this.invoiceRepository = invoiceRepository;
        this.emailService = emailService;
        this.paymentLinkGenerator = paymentLinkGenerator;

    }

    @PostConstruct
    public void init() {
        this.squareClient = new SquareClient.Builder()
                .environment(Environment.SANDBOX) // or Environment.PRODUCTION
                .accessToken(squareAccessToken)
                .build();
    }

    @Transactional
    @Override //Checking for all invoice information
    public void validateInvoice(Invoice invoice) {
        if (invoice.getFirstName() == null || invoice.getFirstName().isEmpty()) {
            throw new InvalidInvoiceException("Client first name is required");
        }
        if (invoice.getLastName() == null || invoice.getLastName().isEmpty()) {
            throw new InvalidInvoiceException("Client last name is required");
        }
        if (invoice.getClientEmail() == null || invoice.getClientEmail().isEmpty()) {
            throw new InvalidInvoiceException("Email is required");
        }
        if (invoice.getStreet() == null || invoice.getStreet().isEmpty()) {
            throw new InvalidInvoiceException("Address is required");
        }
        if (invoice.getCity() == null || invoice.getCity().isEmpty()) {
            throw new InvalidInvoiceException("City is required");
        }
        if (invoice.getState() == null || invoice.getState().isEmpty()) {
            throw new InvalidInvoiceException("State is required");
        }
        if (invoice.getZipcode() == 0) {
            throw new InvalidInvoiceException("Zipcode is required");
        }
        if (invoice.getDate() == null) {
            throw new InvalidInvoiceException("Date is required");
        }
        if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
            throw new InvalidInvoiceException("Service/Product is required");
        }
    }

    @Transactional
    @Override
    public String create(Invoice invoice, String idempotencyKey) {
        // Create line items from invoice items
        List<OrderLineItem> lineItems = invoice.getItems().stream()
                .map(item -> new OrderLineItem.Builder("1")
                        .name(item.getName())
                        .basePriceMoney(new Money.Builder()
                                .amount((long) (item.getPrice() * 100))
                                .currency("USD")
                                .build())
                        .build())
                .collect(Collectors.toList());

        // Create order
        Order order = new Order.Builder(squareLocationId)
                .lineItems(lineItems)
                .build();
        CreateOrderRequest createOrderRequest = new CreateOrderRequest.Builder()
                .order(order)
                .build();

        String orderId = null;

        try {
            CreateOrderResponse createOrderResponse = squareClient.getOrdersApi().createOrder(createOrderRequest);
            // Get orderId from response
            orderId = createOrderResponse.getOrder().getId();
            System.out.println("Order Id from Square API: " + orderId);
            // Set orderId in invoice
            invoice.setOrderId(orderId);
        } catch (ApiException e) {
            System.out.println("Errors: " + e.getErrors());
            throw new RuntimeException("Failed to create order", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // set invoice status to UNPAID
        invoice.setStatus(PaymentStatus.UNPAID);

        // save invoice to database
        invoiceRepository.save(invoice);

        // generate payment link
        String paymentLink = paymentLinkGenerator.generatePaymentLink(invoice, idempotencyKey, order);

        // send payment link to user
        if (invoice.getClientEmail() != null) {
            String subject = "Your Invoice Payment Link";
            String body = "Here is your payment link: " + paymentLink;
            emailService.sendEmail(invoice.getClientEmail(), subject, body);
        }

        // make an asynchronous call using Square SDK
        squareClient.getLocationsApi().listLocationsAsync()
                .thenAccept(result -> {
                    System.out.println("Successfully called List Locations");
                    System.out.println("Request:\n" + result.getContext().getRequest());
                    System.out.println("Response:\n" + result.getContext().getResponse());
                })
                .exceptionally(exception -> {
                    System.out.println("Failed to make the request");
                    try {
                        throw exception.getCause();
                    } catch (ApiException ae) {
                        System.out.println("ApiException occurred");
                        for (Error err : ae.getErrors()) {
                            System.out.println(err.getCategory());
                            System.out.println(err.getCode());
                            System.out.println(err.getDetail());
                        }
                    } catch (Throwable t) {
                        System.out.println("Other exception occurred");
                        t.printStackTrace();
                    }
                    return null;
                })
                .join();

        return paymentLink;
    }

    @Transactional
    @Override
    public void completePayment(Invoice invoice, String paymentStatus) {
        // check if payment was successful
        if ("COMPLETED".equals(paymentStatus)) {
            // update invoice status
            invoice.setStatus(PaymentStatus.PAID);
            log.info("Invoice status updated to PAID");
        }else if ("FAILED".equals(paymentStatus)) {
            // update invoice status
            invoice.setStatus(PaymentStatus.FAILED);
        }

        // save invoice to database
        invoiceRepository.save(invoice);
        log.info("Invoice saved in database: " + invoice);
        Optional<Invoice> savedInvoice = invoiceRepository.findById(invoice.getId());
        log.info("Retrieved saved invoice from database: " + savedInvoice);

        // send invoice to user
        if (invoice.getClientEmail() != null) {
            String subject = "Your Invoice";
            String body = "Thank you for your payment. Here is your invoice: ...";
            emailService.sendEmail(invoice.getClientEmail(), subject, body);
        } else if (invoice.getPhoneNumber() != null) {
            // send SMS to user with invoice
        }
    }


    @Override
    public void deleteInvoice(Invoice invoice, User user) {

    }

    @Override
    public List<Invoice> getAllInvoices(User user) {
        return null;
    }

    @Override
    public Invoice getInvoiceById(Long id) {
        return null;
    }

    @Override
    public Invoice update(User user, Invoice invoice) {
        return null;
    }

}
