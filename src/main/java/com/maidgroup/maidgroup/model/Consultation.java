package com.maidgroup.maidgroup.model;

import com.maidgroup.maidgroup.model.consultationinfo.ConsultationStatus;
import com.maidgroup.maidgroup.model.consultationinfo.PreferredContact;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
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

}
