package com.maidgroup.maidgroup.service.impl;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.maidgroup.maidgroup.dao.ConsultationRepository;
import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.Consultation;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.consultationinfo.ConsultationStatus;
import com.maidgroup.maidgroup.model.userinfo.Role;
import com.maidgroup.maidgroup.service.ConsultationService;
import com.maidgroup.maidgroup.service.exceptions.ConsultationAlreadyExists;
import com.maidgroup.maidgroup.service.exceptions.ConsultationNotFoundException;
import com.maidgroup.maidgroup.service.exceptions.UnauthorizedException;
import com.maidgroup.maidgroup.service.exceptions.UserNotFoundException;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.io.FileReader;

public class ConsultationServiceImpl implements ConsultationService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    ConsultationRepository consultRepository;

    @Override
    public Consultation create(Consultation consultation) {
        int id = consultation.getId();
        Consultation retrievedConsultation = consultRepository.findById(id).orElse(null);
        if(retrievedConsultation!=null){
            throw new ConsultationAlreadyExists("Consultation already exists");
        }
        EmailValidator emailValidator = EmailValidator.getInstance();
        if (!emailValidator.isValid(consultation.getEmail())) {
            throw new RuntimeException("Invalid email address");
        }
        if(consultation.getFirstName().isEmpty()){
            throw new RuntimeException("First name cannot be empty");
        }
        if(consultation.getLastName().isEmpty()){
            throw new RuntimeException("Last name cannot be empty");
        }
        if(consultation.getPhoneNumber().isEmpty()){
            throw new RuntimeException("Must enter a phone number");
        }
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(consultation.getPhoneNumber(), "US");
            if (!phoneNumberUtil.isValidNumber(phoneNumber)) {
                throw new RuntimeException("Invalid phone number");
            }
        } catch (NumberParseException e) {
            throw new RuntimeException("Failed to parse phone number", e);
        }
        if(consultation.getPreferredContact()==null){
            throw new RuntimeException("Must select a preferred contact method");
        }
        if(consultation.getDate()==null){
            throw new RuntimeException("Must select a date for your consultation");
        }
        if(consultation.getTime()==null){
            throw new RuntimeException("Must select a time for your consultation");
        }
        consultation.getMessage();

        consultRepository.save(consultation);
        consultation.setStatus(ConsultationStatus.OPEN);

        String clientMessage = "Your consultation has been booked! We will contact you shortly to confirm details. \n"+consultation.toString()+" \n Notifications regarding your consultation will be sent via SMS. Reply CANCEL to cancel your consultation.";
        String adminMessage = "The following consultation has been booked: \n"+consultation.toString();

        TwilioSMS.sendSMS(consultation.getPhoneNumber(), clientMessage);
        TwilioSMS.sendSMS("+3019384728", adminMessage);

        return consultation;
    }

    @Override
    public void delete(User user, Consultation consultation) {
        Optional<User> userOptional = userRepository.findById(user.getUsername());
        Optional<Consultation> consultOptional = consultRepository.findById(consultation.getId());

        if(userOptional.isPresent() && consultOptional.isPresent()){
            User retrievedUser = userOptional.get();
            Consultation retrievedConsult = consultOptional.get();

            if (retrievedUser.getUsername().equals(consultation.getUser().getUsername()) || retrievedUser.getRole() == Role.Admin) {
                consultRepository.delete(retrievedConsult);
            }
        }
    }

    @Override
    public List<Consultation> getAllConsults(User user) {
        return null;
    }

    @Override
    public List<Consultation> getOpenConsults(User user, Consultation consultation) {
        return null;
    }

    @Override
    public Consultation getConsultById(User user, int id, Consultation consultation) {
        Optional<Consultation> optionalConsultation = consultRepository.findById(id);
        Optional<User> optionalUser = userRepository.findById(user.getUsername());
        if(!optionalConsultation.isPresent()){
            throw new ConsultationNotFoundException("No consultation was found.");
        }
        if(!optionalUser.isPresent()){
            throw new UserNotFoundException("No user was found.");
        }
        Consultation retrievedConsultation = optionalConsultation.get();
        User retrievedUser = optionalUser.get();

        if (!retrievedUser.getUsername().equals(retrievedConsultation.getUser().getUsername()) || retrievedUser.getRole()!=Role.Admin){
            throw new UnauthorizedException("You are not authorized to view this consultation.");
        }
        return retrievedConsultation;
    }

    @Override
    public List<Consultation> getConsultByDate(User user) {
        return null;
    }

    @Override
    public Consultation update(User user, Consultation consultation) {
        return null;
    }

    @Override
    public void setConsultStatus() {

    }

public static class TwilioSMS {
    private static final Properties properties;

        static {
            properties = new Properties();
            try (InputStream inputStream = TwilioSMS.class.getResourceAsStream("/db.properties")) {
                properties.load(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    //Twilio account credentials
        public static final String ACCOUNT_SID = "ACe51c2a222e085880b8415842a0d5db9d";
        public static final String AUTH_TOKEN = properties.getProperty("authenticationtoken");;
        public static final String FROM_NUMBER = "+18446241944";



        public static void sendSMS(String to, String messageBody) {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            Message message = Message.creator(new PhoneNumber(to), new PhoneNumber(FROM_NUMBER), messageBody).create();
            System.out.println("SMS sent: " + message.getSid());
    }
}

}

