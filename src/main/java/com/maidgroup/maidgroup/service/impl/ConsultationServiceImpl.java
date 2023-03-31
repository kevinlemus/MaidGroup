package com.maidgroup.maidgroup.service.impl;

import com.maidgroup.maidgroup.dao.ConsultationRepository;
import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.Consultation;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.userinfo.Role;
import com.maidgroup.maidgroup.service.ConsultationService;
import com.maidgroup.maidgroup.service.exceptions.ConsultationNotFoundException;
import com.maidgroup.maidgroup.service.exceptions.UnauthorizedException;
import com.maidgroup.maidgroup.service.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class ConsultationServiceImpl implements ConsultationService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    ConsultationRepository consultRepository;

    @Override
    public Consultation create(Consultation consultation) {
        return null;
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
}
