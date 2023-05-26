package com.maidgroup.maidgroup.security;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Password {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String hashedPassword;
    @Column(name = "date_last_used", columnDefinition = "DATE")
    private LocalDate dateLastUsed;

    public Password(String hashedPassword, LocalDate dateLastUsed) {
        this.hashedPassword = hashedPassword;
        this.dateLastUsed = dateLastUsed;
    }

    public Password(String hashedPassword){
        this.hashedPassword = hashedPassword;
    }

    public Password(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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
