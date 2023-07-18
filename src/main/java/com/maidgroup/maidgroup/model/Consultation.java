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
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String message;
    private LocalDate date;
    private LocalTime time;
    @Enumerated(EnumType.STRING)
    PreferredContact preferredContact;
    @Enumerated(EnumType.STRING)
    private ConsultationStatus status;
    private String uniqueLink;
}
