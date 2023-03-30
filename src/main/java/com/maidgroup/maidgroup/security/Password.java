package com.maidgroup.maidgroup.security;

import java.time.LocalDate;

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

}
