package com.maidgroup.maidgroup.service;

import com.maidgroup.maidgroup.model.Consultation;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.consultationinfo.ConsultationStatus;
import com.maidgroup.maidgroup.model.consultationinfo.PreferredContact;
import com.maidgroup.maidgroup.util.dto.Responses.ConsultResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

public interface ConsultationService {

    Consultation create(Consultation consultation);

    List<Consultation> getConsults(User requester, LocalDate date, ConsultationStatus status, PreferredContact preferredContact, String name, String sort);

    Consultation getConsultById(Long id);

    void delete(Long consultId, User requester);

    void deleteConsultations(User requester, List<Long> ids);

    void cancelConsultation(Long consultId, String from, String body);

    void cancelConsultationUniqueLink(String uniqueLink);



/*

    List<Consultation> getConsultByDate(User user, LocalDate date, String sort);

    List<ConsultResponse> getAllConsults(User user);

    List<Consultation> getConsultByStatus(User user, ConsultationStatus status);
 */

}