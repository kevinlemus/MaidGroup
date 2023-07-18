package com.maidgroup.maidgroup.service.impl;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.maidgroup.maidgroup.dao.ConsultationRepository;
import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.Consultation;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.consultationinfo.ConsultationStatus;
import com.maidgroup.maidgroup.model.consultationinfo.PreferredContact;
import com.maidgroup.maidgroup.model.userinfo.Role;
import com.maidgroup.maidgroup.service.ConsultationService;
import com.maidgroup.maidgroup.service.exceptions.*;
import com.maidgroup.maidgroup.util.dto.Responses.ConsultResponse;
import com.maidgroup.maidgroup.util.twilio.TwilioSMS;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.attribute.UserPrincipal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConsultationServiceImpl implements ConsultationService {

    UserRepository userRepository;
    ConsultationRepository consultRepository;
    TwilioSMS twilioSMS;

    @Autowired
    public ConsultationServiceImpl(UserRepository userRepository, ConsultationRepository consultRepository, TwilioSMS twilioSMS) {
        this.userRepository = userRepository;
        this.consultRepository = consultRepository;
        this.twilioSMS = twilioSMS;
    }

    @Transactional
    @Override
    public Consultation create(Consultation consultation) {
        Long id = consultation.getId();
        Consultation retrievedConsultation = consultRepository.findById(id).orElse(null);
        if(retrievedConsultation!=null){
            throw new ConsultationAlreadyExists("Consultation already exists");
        }
        EmailValidator emailValidator = EmailValidator.getInstance();
        if (!emailValidator.isValid(consultation.getEmail())) {
            throw new InvalidEmailException("Invalid email address");
        }
        if(consultation.getFirstName().isEmpty()){
            throw new InvalidNameException("First name cannot be empty");
        }
        if(consultation.getLastName().isEmpty()){
            throw new InvalidNameException("Last name cannot be empty");
        }
        if(consultation.getPhoneNumber().isEmpty()){
            throw new InvalidPhoneNumberException("Must enter a phone number");
        }
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(consultation.getPhoneNumber(), "US");
            if (!phoneNumberUtil.isValidNumber(phoneNumber)) {
                throw new InvalidPhoneNumberException("Invalid phone number");
            }
        } catch (NumberParseException e) {
            throw new InvalidPhoneNumberException("Failed to parse phone number");
        }
        if(consultation.getPreferredContact()==null){
            throw new NullPreferredContactException("Must select a preferred contact method");
        }
        if(consultation.getDate()==null){
            throw new InvalidDateException("Must select a date for your consultation");
        }
        if(consultation.getTime()==null){
            throw new InvalidTimeException("Must select a time for your consultation");
        }

        consultRepository.save(consultation);
        consultation.setStatus(ConsultationStatus.OPEN);

        String uniqueLink = generateUniqueLink();
        consultation.setUniqueLink(uniqueLink);
        consultRepository.save(consultation);

        String clientMessage = "Your consultation has been booked! We will contact you shortly to confirm details. \n"+consultation+" \n Notifications regarding your consultation will be sent via SMS. Reply CANCEL to cancel your consultation.";
        String adminMessage = "The following consultation has been booked: \n"+consultation;

        twilioSMS.sendSMS(consultation.getPhoneNumber(), clientMessage);
        twilioSMS.sendSMS("+3019384728", adminMessage);

        if (consultation.getPreferredContact().equals(PreferredContact.Email)) {
            String emailMessage = "Your consultation has been booked! We will contact you shortly to confirm details. Here is your unique link to view or cancel your consultation: " + uniqueLink;
            // send email to user
        }

        return consultation;
    }

    @Transactional
    @Override
    public Consultation getConsultById(Long id) {
        Optional<Consultation> consultation = consultRepository.findById(id);
        if(!consultation.isPresent()){
            throw new ConsultationNotFoundException("No consultation was found.");
        }
        Consultation retrievedConsultation = consultation.get();
        return retrievedConsultation;
    }

    @Transactional
    @Override
    public List<Consultation> getConsults(User requester, LocalDate date, ConsultationStatus status, PreferredContact preferredContact, String name, String sort) {
        List<Consultation> consultations = consultRepository.findAll();
        if (date != null) {
            consultations = consultations.stream()
                    .filter(consultation -> consultation.getDate().equals(date))
                    .collect(Collectors.toList());
        }
        if (status != null) {
            consultations = consultations.stream()
                    .filter(consultation -> consultation.getStatus().equals(status))
                    .collect(Collectors.toList());
        }
        if (preferredContact != null) {
            consultations = consultations.stream()
                    .filter(consultation -> consultation.getPreferredContact().equals(preferredContact))
                    .collect(Collectors.toList());
        }
        if (name != null) {
            consultations = consultations.stream()
                    .filter(consultation -> consultation.getFirstName().contains(name) || consultation.getLastName().contains(name))
                    .collect(Collectors.toList());
        }
        Optional<User> user = userRepository.findById(requester.getUserId());
        boolean isAdmin = requester.getRole().equals(Role.ADMIN);

        if (user.isPresent()) {
            if (isAdmin) {
                if (consultations.isEmpty()) {
                    throw new ConsultationNotFoundException("No consultations were found.");
                }
                if (sort != null) {
                    switch (sort) {
                        case "recent":
                            consultations.sort(Comparator.comparing(Consultation::getDate).reversed());
                            break;
                        case "oldest":
                            consultations.sort(Comparator.comparing(Consultation::getDate));
                            break;
                        case "nameAsc":
                            consultations.sort(Comparator.comparing(Consultation::getLastName)
                                    .thenComparing(Consultation::getFirstName));
                            break;
                        case "nameDesc":
                            consultations.sort(Comparator.comparing(Consultation::getLastName)
                                    .thenComparing(Consultation::getFirstName).reversed());
                            break;
                        case "statusAsc":
                            consultations.sort(Comparator.comparing(Consultation::getStatus));
                            break;
                        case "statusDesc":
                            consultations.sort(Comparator.comparing(Consultation::getStatus).reversed());
                            break;
                        case "contactAsc":
                            consultations.sort(Comparator.comparing(Consultation::getPreferredContact));
                            break;
                        case "contactDesc":
                            consultations.sort(Comparator.comparing(Consultation::getPreferredContact).reversed());
                            break;
                    }
                }
                return consultations;
            } else {
                throw new UnauthorizedException("You are not authorized to retrieve these consultations.");
            }
        } else {
            throw new UserNotFoundException("No user was found.");
        }
    }

    @Transactional
    @Override
    public void delete(Long consultId, User requester) {
        Optional<Consultation> consultToDelete = consultRepository.findById(consultId);
        boolean isAdmin = requester.getRole().equals(Role.ADMIN);
        if(consultToDelete.isPresent()){
            if (isAdmin) {
                consultRepository.delete(consultToDelete.get());
            } else {
                throw new UnauthorizedException("You are not authorized to delete consultations.");
            }
        } else {
            throw new ConsultationNotFoundException("No consultation with the id "+ consultId +"exists.");
        }
    }

    @Transactional
    @Override
    public void deleteConsultations(User requester, List<Long> ids) {
        Optional<User> user = userRepository.findById(requester.getUserId());
        boolean isAdmin = requester.getRole().equals(Role.ADMIN);

        if (user.isPresent()) {
            if (isAdmin) {
                consultRepository.deleteAllById(ids);
            } else {
                throw new UnauthorizedException("You are not authorized to delete these consultations.");
            }
        } else {
            throw new UserNotFoundException("No user was found.");
        }
    }

    @Transactional
    @Override
    public void cancelConsultation(Long consultId, String from, String body) {
        Optional<Consultation> optionalConsultation;
        if (consultId != null) {
            optionalConsultation = consultRepository.findById(consultId);
        } else if (from != null) {
            optionalConsultation = consultRepository.findByPhoneNumber(from);
            if (!optionalConsultation.isPresent()) {
                throw new ConsultationNotFoundException("There is no consultation associated with this phone number.");
            }
            if (!body.equalsIgnoreCase("CANCEL")) {
                throw new InvalidSmsMessageException("Invalid message.");
            }
        } else {
            throw new ConsultationNotFoundException("No consultation was found.");
        }
        if(!optionalConsultation.isPresent()){
            throw new ConsultationNotFoundException("There is no consultation associated with this id or phone number.");
        }
        Consultation consultation = optionalConsultation.get();
        consultation.setStatus(ConsultationStatus.CANCELLED);
        consultRepository.save(consultation);
        String clientMessage = "Your consultation has successfully been cancelled. Thank you for considering our services.";
        String adminMessage = "The following consultation has been cancelled: \n" + consultation;
        twilioSMS.sendSMS(from, clientMessage);
        twilioSMS.sendSMS("+3019384728", adminMessage);
    }

    @Transactional
    @Override
    public void cancelConsultationUniqueLink(String uniqueLink) {
        Optional<Consultation> optionalConsultation = consultRepository.findByUniqueLink(uniqueLink);
        if (!optionalConsultation.isPresent()) {
            throw new ConsultationNotFoundException("There is no consultation associated with this unique link.");
        }
        Consultation consultation = optionalConsultation.get();
        consultation.setStatus(ConsultationStatus.CANCELLED);
        consultRepository.save(consultation);
        String clientMessage = "Your consultation has successfully been cancelled. Thank you for considering our services.";
        String adminMessage = "The following consultation has been cancelled: \n" + consultation;
        // send email to user
        twilioSMS.sendSMS("+3019384728", adminMessage);
    }

    public String generateUniqueLink() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        String uniqueLink = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return uniqueLink;
    }



/*
    @Override
    public List<ConsultResponse> getAllConsults(User user) {
        if(!user.getRole().equals(Role.ADMIN)){
            throw new UnauthorizedException("You are not authorized to view all consultations.");
        }
        List<Consultation> allConsultations = consultRepository.findAll();
        if(allConsultations.isEmpty()) {
            throw new UserNotFoundException("No consultations were found.");
        }
        return allConsultations.stream().map(ConsultResponse::new).collect(Collectors.toList());
    }

    @Override
    public List<Consultation> getConsultByStatus(User requester, ConsultationStatus status) {
        List<Consultation> consultations = consultRepository.findByStatus(status);
        Optional<User> user = userRepository.findById(requester.getUserId());
        boolean isAdmin = requester.getRole().equals(Role.ADMIN);
        if(user.isPresent()) {
            if (isAdmin) {
                if(consultations.isEmpty()){
                    throw new ConsultationNotFoundException("No consultations with the status " + status + "were found.");
                }
                return consultations;
            } else {
                throw new UnauthorizedException("You are not authorized to retrieve these consultations.");
            }
        }else {
            throw new UserNotFoundException("No user was found.");
        }
    }
    @Override
    public List<Consultation> getConsultByDate(User requester, LocalDate date, String sort) {
        List<Consultation> consultations;
        if (date != null) {
            consultations = consultRepository.findByDate(date);
        } else {
            consultations = consultRepository.findAll();
        }
        Optional<User> user = userRepository.findById(requester.getUserId());
        boolean isAdmin = requester.getRole().equals(Role.ADMIN);

        if (user.isPresent()) {
            if (isAdmin) {
                if (consultations.isEmpty()) {
                    throw new ConsultationNotFoundException("No consultations with the date " + date + " were found.");
                }
                if (sort != null) {
                    switch (sort) {
                        case "recent":
                            consultations.sort(Comparator.comparing(Consultation::getDate).reversed());
                            break;
                        case "oldest":
                            consultations.sort(Comparator.comparing(Consultation::getDate));
                            break;
                        case "today":
                            LocalDate today = LocalDate.now();
                            consultations = consultations.stream()
                                    .filter(consultation -> consultation.getDate().equals(today))
                                    .collect(Collectors.toList());
                            break;
                    }
                }
                return consultations;
            } else {
                throw new UnauthorizedException("You are not authorized to retrieve these consultations.");
            }
        } else {
            throw new UserNotFoundException("No user was found.");
        }
    }
 */
}

