package com.maidgroup.maidgroup.util.dto.Requests;

import com.maidgroup.maidgroup.model.invoiceinfo.InvoiceItem;
import com.maidgroup.maidgroup.model.invoiceinfo.PaymentStatus;
import jakarta.validation.constraints.*;
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
    @NotNull
    @NotBlank
    private String street;
    @NotNull
    @NotBlank
    private String city;
    @NotNull
    @NotBlank
    private String state;
    @Min(10000)
    @Max(99999)
    private int zipcode;
    @NotNull
    private LocalDate date;
    @NotNull
    @NotBlank
    private String firstName;
    @NotNull
    @NotBlank
    private String lastName;
    @Email
    @NotNull
    @NotBlank
    private String clientEmail;
    private String phoneNumber;
    private double totalPrice;
    private PaymentStatus status;
    @NotEmpty
    private List<InvoiceItem> items;
}

