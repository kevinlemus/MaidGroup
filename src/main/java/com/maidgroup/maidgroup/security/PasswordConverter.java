package com.maidgroup.maidgroup.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;
@Converter(autoApply = true)
public class PasswordConverter implements AttributeConverter<PasswordEmbeddable, String> {

    @Override
    public String convertToDatabaseColumn(PasswordEmbeddable attribute) {
        return attribute.toString();
    }

    @Override
    public PasswordEmbeddable convertToEntityAttribute(String passwordString) {
        return new PasswordEmbeddable(passwordString);
    }

}
