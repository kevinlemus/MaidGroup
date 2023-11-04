package com.maidgroup.maidgroup.util.scheduled;

import com.maidgroup.maidgroup.dao.ConsultationRepository;
import com.maidgroup.maidgroup.model.Consultation;
import com.maidgroup.maidgroup.model.consultationinfo.ConsultationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ConsultationStatusUpdater {

    /* We are updating the status of our consultations to either "HAPPENING NOW" if it is happening in the present or
    "CLOSED" if it has been an hour since the consultation and the consultation has not been manually set to closed. */

    @Autowired
    private ConsultationRepository consultRepository;

    //The fixed rate determines how often this method is to be invoked. 60,000 in milliseconds translates to 60 seconds/1 minute.
    //This method is scheduled to be invoked once every minute.
    @Scheduled(fixedRate = 60000)
    public void updateConsultationStatuses() {
        LocalDateTime now = LocalDateTime.now();
        List<Consultation> consultations = consultRepository.findAll();
        for (Consultation consultation : consultations) {
            LocalDateTime consultationTime = LocalDateTime.of(consultation.getDate(), consultation.getTime());
            if (consultationTime.isBefore(now) && consultation.getStatus() != ConsultationStatus.HAPPENING_NOW) {
                consultation.setStatus(ConsultationStatus.HAPPENING_NOW);
                consultRepository.save(consultation);
            } else if (consultationTime.plusHours(1).isBefore(now) && consultation.getStatus() != ConsultationStatus.CLOSED) {
                consultation.setStatus(ConsultationStatus.CLOSED);
                consultRepository.save(consultation);
            }
        }
    }
}
