package com.maidgroup.maidgroup.service;

import com.maidgroup.maidgroup.model.Consultation;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.consultationinfo.ConsultationStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface ConsultationService {

    Consultation create(Consultation consultation);
    void delete(User user, Consultation consultation);
    List<Consultation> getAllConsults(Consultation consultation);
    List<Consultation> getConsultByStatus(User user, Consultation consultation, ConsultationStatus status);
    Consultation getConsultById(User user, int id, Consultation consultation);
    List<Consultation> getConsultByDate(User user, Consultation consultation, LocalDate date);
    Consultation update(User user, Consultation consultation);
    void cancelConsultation(String from, String body);

}
