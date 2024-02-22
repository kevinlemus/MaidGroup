package com.maidgroup.maidgroup.security;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@Data
@Embeddable
public class Password {
    private String hashedPassword;
    private LocalDate dateLastUsed;
    private String resetToken; // This field is added for the password reset functionality

    public Password(String hashedPassword, LocalDate dateLastUsed) {
        this.hashedPassword = hashedPassword;
        this.dateLastUsed = dateLastUsed;
    }

    public Password(String hashedPassword, LocalDate dateLastUsed, String resetToken) {
        this.hashedPassword = hashedPassword;
        this.dateLastUsed = dateLastUsed;
        this.resetToken = resetToken;
    }

    public Password(String hashedPassword){
        this.hashedPassword = hashedPassword;
    }

    public Password(){}
}

