package com.maidgroup.maidgroup.security;

import jakarta.persistence.*;
import java.time.LocalDate;

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

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public LocalDate getDateLastUsed() {
        return dateLastUsed;
    }

    public void setDateLastUsed(LocalDate dateLastUsed) {
        this.dateLastUsed = dateLastUsed;
    }

    @Override
    public String toString() {
        return "Password{" +
                ", hashedPassword='" + hashedPassword + '\'' +
                ", dateLastUsed=" + dateLastUsed +
                '}';
    }
}
