package com.maidgroup.maidgroup.util.dto.Requests;

import com.maidgroup.maidgroup.model.invoiceinfo.InvoiceItem;
import com.maidgroup.maidgroup.model.invoiceinfo.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequest {
    private Long id;
    private String orderId;
    private int zipcode;
    private LocalDate date;
    private String firstName;
    private String lastName;
    private String clientEmail;
    private String phoneNumber;
    private double totalPrice;
    private PaymentStatus status;
    private List<InvoiceItem> items;
}
