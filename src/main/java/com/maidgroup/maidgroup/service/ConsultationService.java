package com.maidgroup.maidgroup.service;

import com.maidgroup.maidgroup.model.Consultation;
import com.maidgroup.maidgroup.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ConsultationService {

    Consultation create(Consultation consultation);
    void delete(User user, Consultation consultation);
    List<Consultation> getAllConsults(User user);
    List<Consultation> getOpenConsults(User user, Consultation consultation);
    Consultation getConsultById(User user, int id, Consultation consultation);
    List<Consultation> getConsultByDate(User user);
    Consultation update(User user, Consultation consultation);
    void setConsultStatus();

}
