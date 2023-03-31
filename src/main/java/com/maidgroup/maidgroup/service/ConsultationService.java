package com.maidgroup.maidgroup.service;

import com.maidgroup.maidgroup.model.Consultation;
import org.apache.catalina.User;

import java.util.List;

public interface ConsultationService {

    Consultation create(Consultation consultation);
    void delete(User user, Consultation consultation);
    List<Consultation> getAllConsults(User user);
    List<Consultation> getOpenConsults(User user, Consultation consultation);
    Consultation getConsultById(User user);
    List<Consultation> getConsultByDate(User user);
    Consultation update(User user, Consultation consultation);
    void setConsultStatus();

}
