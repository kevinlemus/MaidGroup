package com.maidgroup.maidgroup.service.exceptions;

public class ConsultationAlreadyExists extends RuntimeException{
    public ConsultationAlreadyExists(String message){
        super(message);
    }
}
