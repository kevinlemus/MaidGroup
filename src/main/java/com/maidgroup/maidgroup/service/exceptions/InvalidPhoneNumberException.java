package com.maidgroup.maidgroup.service.exceptions;

public class InvalidPhoneNumberException extends RuntimeException{
    public InvalidPhoneNumberException(String message){
        super(message);
    }
}
