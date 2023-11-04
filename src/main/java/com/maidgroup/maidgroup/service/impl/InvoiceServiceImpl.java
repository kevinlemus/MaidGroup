package com.maidgroup.maidgroup.service.impl;

import com.maidgroup.maidgroup.dao.InvoiceRepository;
import com.maidgroup.maidgroup.model.Invoice;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.invoiceinfo.PaymentStatus;
import com.maidgroup.maidgroup.service.EmailService;
import com.maidgroup.maidgroup.service.InvoiceService;
import com.maidgroup.maidgroup.service.exceptions.InvalidInvoiceException;
import com.maidgroup.maidgroup.util.payment.PaymentLinkGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    InvoiceRepository invoiceRepository;
    EmailService emailService;
    private PaymentLinkGenerator paymentLinkGenerator;

    @Autowired
    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, EmailService emailService, PaymentLinkGenerator paymentLinkGenerator) {
        this.invoiceRepository = invoiceRepository;
        this.emailService = emailService;
        this.paymentLinkGenerator = paymentLinkGenerator;
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
    public String create(Invoice invoice) {
        // generate payment link
        String paymentLink = paymentLinkGenerator.generatePaymentLink(invoice);

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
    public void completePayment(Invoice invoice) {
        // save invoice to database
        invoiceRepository.save(invoice);

        // update invoice status
        invoice.setStatus(PaymentStatus.PAID);

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
