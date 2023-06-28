package com.maidgroup.maidgroup.service.exceptions;

public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException (String message) {
        super(message);
    }
}
