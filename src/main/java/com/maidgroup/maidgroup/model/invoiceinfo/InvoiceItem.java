package com.maidgroup.maidgroup.model.invoiceinfo;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InvoiceItem {
    private String name;
    private double price;
    @Enumerated(EnumType.STRING)
    private ItemType type;
}
