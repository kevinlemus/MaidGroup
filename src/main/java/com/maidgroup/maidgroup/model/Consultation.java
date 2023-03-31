package com.maidgroup.maidgroup.model;

import com.maidgroup.maidgroup.model.consultationinfo.ConsultationStatus;
import com.maidgroup.maidgroup.model.consultationinfo.PreferredContact;
import jakarta.persistence.*;

@Entity
public class Consultation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    String message;
    @Enumerated(EnumType.STRING)
    PreferredContact preferredContact;
    @Enumerated(EnumType.STRING)
    private ConsultationStatus status;
    boolean isOpen;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PreferredContact getPreferredContact() {
        return preferredContact;
    }

    public void setPreferredContact(PreferredContact preferredContact) {
        this.preferredContact = preferredContact;
    }

    public ConsultationStatus getStatus() {
        return status;
    }

    public void setStatus(ConsultationStatus status) {
        this.status = status;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }
}
