package com.maidgroup.maidgroup.util.scheduled;

import com.maidgroup.maidgroup.dao.ConsultationRepository;
import com.maidgroup.maidgroup.model.Consultation;
import com.maidgroup.maidgroup.model.consultationinfo.ConsultationStatus;
import com.maidgroup.maidgroup.model.consultationinfo.PreferredContact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ConsultationReminder {

    /* We are sending out reminders to clients when they have upcoming consultations. Once the consultation is in
    24 hours, a reminder is sent out to the client. */

    @Autowired
    private ConsultationRepository consultRepository;

    //The fixed rate determines how often this method is to be invoked. 3,600,000 in milliseconds translates to 60 minutes/1 hour.
    //This method is scheduled to be invoked once every hour.
    @Scheduled(fixedRate = 3600000)
    public void sendConsultationReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Consultation> consultations = consultRepository.findAll();
        for (Consultation consultation : consultations) {
            LocalDateTime consultationTime = LocalDateTime.of(consultation.getDate(), consultation.getTime());
            if (consultationTime.minusHours(24).isBefore(now) && consultation.getStatus() == ConsultationStatus.OPEN) {
                String clientMessage = "Your consultation is coming up! We'll be happy to see you tomorrow at " + consultation.getTime().format(DateTimeFormatter.ofPattern("h:mm a")) + ". If you'd like to make any changes, here is your unique link: " + consultation.getUniqueLink();
                if (consultation.getPreferredContact() == PreferredContact.Email) {
                    // send email to user
                } else {
                    // send SMS to user
                }
            }
        }
    }
}
