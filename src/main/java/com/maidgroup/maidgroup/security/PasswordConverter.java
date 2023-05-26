package com.maidgroup.maidgroup.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;
@Converter(autoApply = true)
public class PasswordConverter implements AttributeConverter<Password, String> {

    @Override
    public String convertToDatabaseColumn(Password password) {
        return password.toString();
    }

    @Override
    public Password convertToEntityAttribute(String passwordString) {
        return new Password(passwordString);
    }
}
