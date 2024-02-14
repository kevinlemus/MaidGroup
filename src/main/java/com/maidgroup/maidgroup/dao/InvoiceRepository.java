package com.maidgroup.maidgroup.dao;

import com.maidgroup.maidgroup.model.Invoice;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.invoiceinfo.PaymentStatus;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Invoice findByOrderId(String orderId);
    List<Invoice> findByUser(User user);
    List<Invoice> findByStatusAndDateBefore(PaymentStatus status, LocalDate date);
}


