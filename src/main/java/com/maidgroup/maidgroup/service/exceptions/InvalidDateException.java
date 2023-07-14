package com.maidgroup.maidgroup.service.exceptions;

public class InvalidDateException extends RuntimeException{
    public InvalidDateException (String message) {
        super(message);
    }
}
