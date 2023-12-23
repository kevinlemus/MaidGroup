package com.maidgroup.maidgroup.service.impl;

import com.maidgroup.maidgroup.dao.InvoiceRepository;
import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.Consultation;
import com.maidgroup.maidgroup.model.Invoice;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.invoiceinfo.PaymentStatus;
import com.maidgroup.maidgroup.model.userinfo.Role;
import com.maidgroup.maidgroup.service.EmailService;
import com.maidgroup.maidgroup.service.InvoiceService;
import com.maidgroup.maidgroup.service.exceptions.*;
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

import javax.swing.text.html.Option;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@Service
public class InvoiceServiceImpl implements InvoiceService {

    InvoiceRepository invoiceRepository;
    UserRepository userRepository;
    EmailService emailService;
    private PaymentLinkGenerator paymentLinkGenerator;
    private SquareClient squareClient;
    @Value("${square.location-id}")
    private String squareLocationId;
    @Value("${square.access-token}")
    private String squareAccessToken;

    @Autowired
    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, UserRepository userRepository, EmailService emailService, PaymentLinkGenerator paymentLinkGenerator) {
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
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
    public SquareClient getSquareClient() {
        return this.squareClient;
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
        String orderId = UUID.randomUUID().toString();
        invoice.setOrderId(orderId);
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

        System.out.println("The invoice order ID set in create: " + invoice.getOrderId());

        // Create order
        Order order = new Order.Builder(squareLocationId)
                .referenceId(invoice.getOrderId())
                .lineItems(lineItems)
                .build();

        System.out.println("The reference id order ID set in create: " + order.getReferenceId());

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

            // save invoice to database
            invoiceRepository.save(invoice);

            // send invoice to user only if it hasn't been sent before
            if (!invoice.isSent()) {
                if (invoice.getClientEmail() != null) {
                    String subject = "Your Invoice";
                    String body = "Thank you for your payment. Here is your invoice: ...";
                    emailService.sendEmail(invoice.getClientEmail(), subject, body);
                } else if (invoice.getPhoneNumber() != null) {
                    // send SMS to user with invoice
                }
                // mark the invoice as sent
                invoice.setSent(true);
                invoiceRepository.save(invoice);
            }
        } else if ("FAILED".equals(paymentStatus)) {
            // update invoice status
            invoice.setStatus(PaymentStatus.FAILED);
            invoiceRepository.save(invoice);
        }
    }

    @Transactional
    @Override
    public void delete(Long invoiceId, User requester) {
        Optional<Invoice> invoiceToDelete = invoiceRepository.findById(invoiceId);
        boolean isAdmin = requester.getRole().equals(Role.ADMIN);
        if(invoiceToDelete.isPresent()){
            if(isAdmin) {
                invoiceRepository.delete(invoiceToDelete.get());
            } else {
                throw new UnauthorizedException("You are not authorized to delete consultations.");
            }
        } else {
            throw new InvoiceNotFoundException("No invoice with the id " + invoiceId + "exists.");
        }
    }

    @Transactional
    @Override
    public List<Invoice> getInvoices(User requester, LocalDate date, PaymentStatus status, String sort) {
        List<Invoice> invoices;
        boolean isAdmin = requester.getRole().equals(Role.ADMIN);

        if (isAdmin) {
            // If the requester is an admin, get all invoices
            invoices = invoiceRepository.findAll();
        } else {
            // If the requester is not an admin, get only their invoicesSSS
            invoices = invoiceRepository.findByUser(requester);
        }

        if (date != null) {
            invoices = invoices.stream()
                    .filter(invoice -> invoice.getDate().equals(date))
                    .collect(Collectors.toList());
        }

        if (isAdmin && status != null) {
            invoices = invoices.stream()
                    .filter(invoice -> invoice.getStatus().equals(status))
                    .collect(Collectors.toList());
        }

        if (invoices.isEmpty()) {
            throw new InvoiceNotFoundException("No invoices were found.");
        }

        if (sort != null) {
            switch (sort) {
                case "recent":
                    invoices.sort(Comparator.comparing(Invoice::getDate).reversed());
                    break;
                case "oldest":
                    invoices.sort(Comparator.comparing(Invoice::getDate));
                    break;
                case "nameAsc":
                    invoices.sort(Comparator.comparing(Invoice::getLastName)
                            .thenComparing(Invoice::getFirstName));
                    break;
                case "nameDesc":
                    invoices.sort(Comparator.comparing(Invoice::getLastName)
                            .thenComparing(Invoice::getFirstName).reversed());
                    break;
                case "statusAsc":
                    invoices.sort(Comparator.comparing(Invoice::getStatus));
                    break;
                case "statusDesc":
                    invoices.sort(Comparator.comparing(Invoice::getStatus).reversed());
                    break;
            }
        }

        return invoices;
    }

    @Transactional
    @Override
    public Invoice getInvoiceById(Long id, User requester) {
        Optional<Invoice> invoice = invoiceRepository.findById(id);
        if(invoice.isEmpty()){
            throw new InvoiceNotFoundException("No invoice was found.");
        }
        Invoice retrievedInvoice = invoice.get();

        boolean isAdmin = requester.getRole().equals(Role.ADMIN);
        boolean isOwner = retrievedInvoice.getUser().equals(requester);

        if (isAdmin || isOwner) {
            return retrievedInvoice;
        } else {
            throw new UnauthorizedException("You are not authorized to view this invoice.");
        }
    }

    @Override
    public Invoice update(User user, Invoice invoice) {
        return null;
    }

}
