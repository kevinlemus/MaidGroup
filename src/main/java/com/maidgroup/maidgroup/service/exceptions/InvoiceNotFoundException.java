package com.maidgroup.maidgroup.service.exceptions;

public class InvoiceNotFoundException extends RuntimeException{
    public InvoiceNotFoundException (String message) {
        super(message);
    }
}
