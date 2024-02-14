package com.maidgroup.maidgroup.util.aspects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.maidgroup.maidgroup.service.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ErrorExceptionAspect {


    @ExceptionHandler({UsernameAlreadyExists.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleUsernameAlreadyExistsException(UsernameAlreadyExists e) { return e.getMessage(); }

    @ExceptionHandler({JsonProcessingException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleInvalidJsonProcessingException(JsonProcessingException e){
        return e.getMessage();
    }

    @ExceptionHandler({UserNotFoundException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public String handleUserNotFoundException(UserNotFoundException e) {
        return e.getMessage();
    }

    @ExceptionHandler({UnauthorizedException.class})
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public String handleUnauthorizedException(UnauthorizedException e) {
        return e.getMessage();
    }

    @ExceptionHandler({InvalidCredentialsException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleInvalidCredentialsException(InvalidCredentialsException e) {
        return e.getMessage();
    }

    @ExceptionHandler({InvalidDateException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleInvalidDateException(InvalidDateException e) {
        return e.getMessage();
    }

    @ExceptionHandler({ConsultationAlreadyExists.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleConsultationAlreadyExists(ConsultationAlreadyExists e) {
        return e.getMessage();
    }

    @ExceptionHandler({ConsultationNotFoundException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public String handleConsultationNotFoundException(ConsultationNotFoundException e) {
        return e.getMessage();
    }

    @ExceptionHandler({InvalidEmailException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleInvalidEmailException(InvalidEmailException e) {
        return e.getMessage();
    }

    @ExceptionHandler({InvalidNameException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleInvalidNameException(InvalidNameException e) {
        return e.getMessage();
    }

    @ExceptionHandler({InvalidPhoneNumberException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleInvalidPhoneNumberException(InvalidPhoneNumberException e) {
        return e.getMessage();
    }

    @ExceptionHandler({InvalidSmsMessageException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleInvalidSmsException(InvalidSmsMessageException e) {
        return e.getMessage();
    }

    @ExceptionHandler({InvalidTimeException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleInvalidTimeException(InvalidTimeException e) {
        return e.getMessage();
    }

    @ExceptionHandler({NullPreferredContactException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleNullPreferredContactException(NullPreferredContactException e) {
        return e.getMessage();
    }

    @ExceptionHandler({InvalidInvoiceException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handInvalidInvoiceException(InvalidInvoiceException e) {
        return e.getMessage();
    }
    @ExceptionHandler({InvoiceNotFoundException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public String handleInvoiceNotFoundException(InvoiceNotFoundException e) {
        return e.getMessage();
    }
    @ExceptionHandler({ResponseStatusException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleResponseStatusException(ResponseStatusException e) {
        return e.getMessage();
    }

    @ExceptionHandler({InvoiceAlreadyPaidException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleInvoiceAlreadyPaidException(InvoiceAlreadyPaidException e) {
        return e.getMessage();
    }
}
