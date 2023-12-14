package com.maidgroup.maidgroup.service;

import com.maidgroup.maidgroup.model.Invoice;
import com.maidgroup.maidgroup.model.User;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.util.List;

public interface InvoiceService {
    String create(Invoice invoice, String idempotencyKey);
    void validateInvoice(Invoice invoice);
    void completePayment(Invoice invoice, String paymentStatus);
    void delete(Long invoiceId, User requester);
    List<Invoice> getAllInvoices(User user);
    Invoice getInvoiceById(Long id);
    Invoice update (User user, Invoice invoice);

}
