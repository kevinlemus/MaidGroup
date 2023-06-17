package com.maidgroup.maidgroup.security;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@Data
@Embeddable
public class PasswordEmbeddable {
   @Column(insertable=false, updatable=false)
    private String hashedPassword;
   @Column(insertable=false, updatable=false)
    private LocalDate dateLastUsed;

    public PasswordEmbeddable(String hashedPassword, LocalDate dateLastUsed) {
        this.hashedPassword = hashedPassword;
        this.dateLastUsed = dateLastUsed;
    }

    public PasswordEmbeddable(String hashedPassword){
        this.hashedPassword = hashedPassword;
    }

    public PasswordEmbeddable(){}

}
