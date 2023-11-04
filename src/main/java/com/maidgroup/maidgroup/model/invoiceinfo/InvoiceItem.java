package com.maidgroup.maidgroup.model.invoiceinfo;

import jakarta.persistence.Embeddable;
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
    private ItemType type;
}
