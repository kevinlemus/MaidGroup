package com.maidgroup.maidgroup.service.exceptions;

public class InvalidSmsMessageException extends RuntimeException{
    public InvalidSmsMessageException(String message){
        super(message);
    }
}
