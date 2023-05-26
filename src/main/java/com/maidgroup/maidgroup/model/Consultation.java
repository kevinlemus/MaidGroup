package com.maidgroup.maidgroup.model;

import com.maidgroup.maidgroup.model.consultationinfo.ConsultationStatus;
import com.maidgroup.maidgroup.model.consultationinfo.PreferredContact;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

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
    LocalDate date;
    LocalTime time;
    @Enumerated(EnumType.STRING)
    PreferredContact preferredContact = PreferredContact.Default;
    @Enumerated(EnumType.STRING)
    private ConsultationStatus status;
    @ManyToOne
    @JoinColumn(name = "user_username")
    private User user;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Consultation{" +
                "ID=" + id +
                ", First Name='" + firstName + '\'' +
                ", Last Name='" + lastName + '\'' +
                ", Email='" + email + '\'' +
                ", Phone Number='" + phoneNumber + '\'' +
                ", Message='" + message + '\'' +
                ", Date=" + date +
                ", Time=" + time +
                ", Preferred Contact=" + preferredContact +
                '}';
    }
}
