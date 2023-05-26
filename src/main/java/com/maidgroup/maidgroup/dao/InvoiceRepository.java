package com.maidgroup.maidgroup.dao;

import com.maidgroup.maidgroup.model.Invoice;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
}
