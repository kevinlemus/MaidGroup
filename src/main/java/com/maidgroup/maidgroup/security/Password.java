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

    public Password(String hashedPassword, LocalDate dateLastUsed) {
        this.hashedPassword = hashedPassword;
        this.dateLastUsed = dateLastUsed;
    }

    public Password(String hashedPassword){
        this.hashedPassword = hashedPassword;
    }

    public Password(){}


}
