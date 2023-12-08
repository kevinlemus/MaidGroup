package com.maidgroup.maidgroup.model;

import com.maidgroup.maidgroup.model.invoiceinfo.InvoiceItem;
import com.maidgroup.maidgroup.model.invoiceinfo.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderId;
    @Column(name = "square_order_id")
    private String squareOrderId;
    private String street;
    private String city;
    private String state;
    private int zipcode;
    private LocalDate date;
    private String firstName;
    private String lastName;
    private String clientEmail;
    private String phoneNumber;
    private double totalPrice;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ElementCollection
    private List<InvoiceItem> items;
}
