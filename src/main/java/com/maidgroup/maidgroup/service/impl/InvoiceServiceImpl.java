package com.maidgroup.maidgroup.service.impl;

import com.maidgroup.maidgroup.dao.InvoiceRepository;
import com.maidgroup.maidgroup.model.Invoice;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    InvoiceRepository invoiceRepository;

    @Override
    public Invoice createInvoice(Invoice invoice) {
        Invoice retrievedInvoice = invoiceRepository.findById(invoice.getId()).orElseThrow();
        if(retrievedInvoice!=null){
            throw new RuntimeException("Invoice already exists.");
        }

        return null;
    }

    @Override
    public void deleteInvoice(Invoice invoice, User user) {

    }

    @Override
    public List<Invoice> getAllInvoices(User user) {
        return null;
    }
}
