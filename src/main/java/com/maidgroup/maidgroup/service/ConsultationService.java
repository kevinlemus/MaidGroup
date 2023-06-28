package com.maidgroup.maidgroup.service;

import com.maidgroup.maidgroup.model.Consultation;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.consultationinfo.ConsultationStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

public interface ConsultationService {

    Consultation create(Consultation consultation);
    void delete(User user, Consultation consultation);
    List<Consultation> getAllConsults(User user);
    List<Consultation> getConsultByStatus(User user, ConsultationStatus status);
    Consultation getConsultById(User user, int id, Consultation consultation);
    List<Consultation> getConsultByDate(User user, LocalDate date);
    Consultation update(User user, Consultation consultation);
    void cancelConsultation(String from, String body);

}
