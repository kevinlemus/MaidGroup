package com.maidgroup.maidgroup.service.exceptions;

public class InvalidInvoiceException extends RuntimeException {
    public InvalidInvoiceException (String message) {
        super(message);
    }
}
