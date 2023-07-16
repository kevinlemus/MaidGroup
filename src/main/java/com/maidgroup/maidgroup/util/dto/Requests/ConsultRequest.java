package com.maidgroup.maidgroup.util.dto.Requests;

import com.maidgroup.maidgroup.model.consultationinfo.ConsultationStatus;
import com.maidgroup.maidgroup.model.consultationinfo.PreferredContact;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultRequest {
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
}
