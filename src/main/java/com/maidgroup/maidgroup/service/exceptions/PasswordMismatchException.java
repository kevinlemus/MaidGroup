package com.maidgroup.maidgroup.service.exceptions;

public class PasswordMismatchException extends RuntimeException{
    public PasswordMismatchException(String message){
        super(message);
    }
}
