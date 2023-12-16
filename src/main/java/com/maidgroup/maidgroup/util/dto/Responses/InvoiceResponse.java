package com.maidgroup.maidgroup.util.dto.Responses;

import com.maidgroup.maidgroup.model.Invoice;
import com.maidgroup.maidgroup.model.invoiceinfo.InvoiceItem;
import com.maidgroup.maidgroup.model.invoiceinfo.PaymentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class InvoiceResponse {
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

    public InvoiceResponse(Invoice invoice) {
        this.id = invoice.getId();
        this.orderId = invoice.getOrderId();
        this.zipcode = invoice.getZipcode();
        this.date = invoice.getDate();
        this.firstName = invoice.getFirstName();
        this.lastName = invoice.getLastName();
        this.clientEmail = invoice.getClientEmail();
        this.phoneNumber = invoice.getPhoneNumber();
        this.totalPrice = invoice.getTotalPrice();
        this.status = invoice.getStatus();
        this.items = invoice.getItems();
    }
}
