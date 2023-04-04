package com.maidgroup.maidgroup.controller;

import com.maidgroup.maidgroup.dao.ConsultationRepository;
import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.Consultation;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.consultationinfo.ConsultationStatus;
import com.maidgroup.maidgroup.service.ConsultationService;
import com.maidgroup.maidgroup.service.UserService;
import com.maidgroup.maidgroup.service.exceptions.ConsultationNotFoundException;
import com.maidgroup.maidgroup.service.exceptions.UnauthorizedException;
import com.maidgroup.maidgroup.service.exceptions.UserNotFoundException;
import jdk.jshell.Snippet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.nio.file.attribute.UserPrincipal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/consultation")
@CrossOrigin
public class ConsultationController {

    @Autowired
    ConsultationService consultService;
    @Autowired
    ConsultationRepository consultRepository;
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<Consultation> createConsultation(@RequestBody Consultation consultation){
        consultService.create(consultation);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteConsultation(@PathVariable("id") int id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Optional<Consultation> optionalConsultation = consultRepository.findById(id);
                if (!optionalConsultation.isPresent()) {
                    throw new ConsultationNotFoundException("No consultation was found.");
                }
                Consultation consultation = optionalConsultation.get();
                User user = userService.getByUsername(userDetails.getUsername(), (User) userDetails);
                consultService.delete(user, consultRepository.findById(id).get());
                return new ResponseEntity<>("The consultation has been deleted", HttpStatus.OK);

        } catch (ConsultationNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/allConsultations")
    public ResponseEntity<List<Consultation>> getAllConsultations(@AuthenticationPrincipal UserDetails userDetails){
        try{
            List<Consultation> allConsultations = consultService.getAllConsults(userRepository.findById(userDetails.getUsername()).orElse(null));
            return ResponseEntity.status(HttpStatus.OK).body(allConsultations);
        }catch (ConsultationNotFoundException e){
            return ResponseEntity.notFound().build();
        }catch (UnauthorizedException d){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/{status}")
    public ResponseEntity<List<Consultation>> getConsultByStatus(@PathVariable("status")ConsultationStatus status, @AuthenticationPrincipal UserDetails userDetails){
        try{
            User user = userRepository.findByUsername(userDetails.getUsername());
            List<Consultation> allConsultations = consultService.getConsultByStatus(user, status);
            return ResponseEntity.status(HttpStatus.OK).body(allConsultations);

        }catch (UserNotFoundException e){
            return ResponseEntity.notFound().build();
        }catch (ConsultationNotFoundException d){
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Consultation> getConsultById(@PathVariable("id") int id, @AuthenticationPrincipal UserDetails userDetails){
        Consultation consultation = consultRepository.findById(id).orElse(null);
        return new ResponseEntity<Consultation>(consultService.getConsultById((User) userDetails, id, consultation), HttpStatus.OK);

    }

    @GetMapping("/{date}")
    public ResponseEntity<List<Consultation>> getConsultByDate(@PathVariable("date")LocalDate date, @AuthenticationPrincipal UserDetails userDetails){
        try {
            List<Consultation> consultation = consultRepository.findByDate(date);
            User user = userRepository.findById(userDetails.getUsername()).orElse(null);
            return new ResponseEntity<List<Consultation>>(consultService.getConsultByDate(user, date), HttpStatus.OK);
        }catch (UserNotFoundException u){
            return ResponseEntity.notFound().build();
        }catch (ConsultationNotFoundException c){
            return ResponseEntity.notFound().build();
        }catch (UnauthorizedException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


}
