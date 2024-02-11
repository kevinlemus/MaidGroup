package com.maidgroup.maidgroup.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.maidgroup.maidgroup.model.Invoice;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.invoiceinfo.PaymentStatus;
import com.squareup.square.exceptions.ApiException;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceService {
    String create(Invoice invoice, String idempotencyKey);
    void validateInvoice(Invoice invoice);
    void completePayment(Invoice invoice, String paymentStatus);
    void delete(Long invoiceId, User requester);
    List<Invoice> getInvoices(User requester, LocalDate date, PaymentStatus status, String sort);
    Invoice getInvoiceById(Long id, User requester);
    void sendPaymentLink(Invoice invoice, User user);
    void sendInvoice(Invoice invoice, String email, User user);
    Invoice updateInvoice (User user, Invoice invoice);
    void handleWebhook(String payload) throws IOException, ApiException;
}
