package com.maidgroup.maidgroup.service.exceptions;

public class ConsultationNotFoundException extends RuntimeException{
    public ConsultationNotFoundException(String message){
        super(message);
    }
}
