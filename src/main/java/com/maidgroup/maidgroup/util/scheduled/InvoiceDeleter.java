package com.maidgroup.maidgroup.util.scheduled;

import com.maidgroup.maidgroup.dao.InvoiceRepository;
import com.maidgroup.maidgroup.model.Invoice;
import com.maidgroup.maidgroup.model.invoiceinfo.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class InvoiceDeleter {

    @Autowired
    InvoiceRepository invoiceRepository;

    @Scheduled(cron = "0 0 0 * * ?") // This runs the method every day at midnight
    public void deleteOldUnpaidInvoices() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<Invoice> oldUnpaidInvoices = invoiceRepository.findByStatusAndDateBefore(PaymentStatus.UNPAID, thirtyDaysAgo);
        invoiceRepository.deleteAll(oldUnpaidInvoices);
    }

}
