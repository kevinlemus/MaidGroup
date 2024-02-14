package com.maidgroup.maidgroup.service.exceptions;

public class InvoiceAlreadyPaidException extends RuntimeException{
    public InvoiceAlreadyPaidException(String message){
        super(message);
    }
}
