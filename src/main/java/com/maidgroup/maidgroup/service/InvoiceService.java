package com.maidgroup.maidgroup.service;

import com.maidgroup.maidgroup.model.Invoice;
import com.maidgroup.maidgroup.model.User;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.util.List;

public interface InvoiceService {
    Invoice createInvoice(Invoice invoice);
    void deleteInvoice(Invoice invoice, User user);
    List<Invoice> getAllInvoices(User user);

}
