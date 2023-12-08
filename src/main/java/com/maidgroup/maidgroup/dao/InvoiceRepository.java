package com.maidgroup.maidgroup.dao;

import com.maidgroup.maidgroup.model.Invoice;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Invoice findByOrderId(String orderId);
    Invoice findBySquareOrderId(String squareOrderId);
}

