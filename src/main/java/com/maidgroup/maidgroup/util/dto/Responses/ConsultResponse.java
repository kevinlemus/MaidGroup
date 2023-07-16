package com.maidgroup.maidgroup.util.dto.Responses;

import com.maidgroup.maidgroup.model.Consultation;
import com.maidgroup.maidgroup.model.consultationinfo.ConsultationStatus;
import com.maidgroup.maidgroup.model.consultationinfo.PreferredContact;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
public class ConsultResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String message;
    private LocalDate date;
    private LocalTime time;
    private PreferredContact preferredContact;
    private ConsultationStatus status;

    public ConsultResponse (Consultation consultation){
        this.id = consultation.getId();
        this.firstName = consultation.getFirstName();
        this.lastName = consultation.getLastName();
        this.email = consultation.getEmail();
        this.phoneNumber = consultation.getPhoneNumber();
        this.message = consultation.getMessage();
        this.date = consultation.getDate();
        this.time = consultation.getTime();
        this.preferredContact = consultation.getPreferredContact();
        this.status = consultation.getStatus();
    }
}
