package com.maidgroup.maidgroup.controller;

import com.maidgroup.maidgroup.dao.ConsultationRepository;
import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.Consultation;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.consultationinfo.ConsultationStatus;
import com.maidgroup.maidgroup.model.consultationinfo.PreferredContact;
import com.maidgroup.maidgroup.service.ConsultationService;
import com.maidgroup.maidgroup.service.UserService;
import com.maidgroup.maidgroup.service.exceptions.*;
import com.maidgroup.maidgroup.util.dto.Responses.ConsultResponse;
import jdk.jshell.Snippet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.swing.text.html.Option;
import java.nio.file.attribute.UserPrincipal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/consultation")
@CrossOrigin
public class ConsultationController {

    ConsultationService consultService;
    ConsultationRepository consultRepository;
    UserService userService;
    UserRepository userRepository;

    @Autowired
    public ConsultationController(ConsultationService consultService, ConsultationRepository consultRepository, UserService userService, UserRepository userRepository) {
        this.consultService = consultService;
        this.consultRepository = consultRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/create")
    public ConsultResponse createConsultation(@RequestBody Consultation consultation){
        Consultation consult = consultService.create(consultation);
        ConsultResponse consultResponse = new ConsultResponse(consult);
        return consultResponse;
    }

    @GetMapping("/{id}")
    public ConsultResponse getConsultById(@PathVariable("id") Long id){
        Consultation consultation = consultService.getConsultById(id);
        ConsultResponse consultResponse = new ConsultResponse(consultation);
        return consultResponse;
    }

    @GetMapping
    public @ResponseBody List<ConsultResponse> getConsults(Principal principal, @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestParam(value = "status", required = false) ConsultationStatus status, @RequestParam(value = "preferredContact", required = false) PreferredContact preferredContact, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort) {
        User authUser = userRepository.findByUsername(principal.getName());
        List<Consultation> consultations = consultService.getConsults(authUser, date, status, preferredContact, name, sort);
        return consultations.stream().map(ConsultResponse::new).collect(Collectors.toList());
    }

    @DeleteMapping
    public String deleteConsultations(Principal principal, @RequestParam(value = "ids") List<Long> ids) {
        User authUser = userRepository.findByUsername(principal.getName());
        consultService.deleteConsultations(authUser, ids);
        return "The selected consultations have been deleted.";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") Long id, Principal principal) {
        User authUser = userRepository.findByUsername(principal.getName());
        consultService.delete(id, authUser);
        return "This consultation has been deleted.";
    }

    @Value("${app.cancelEndpointEnabled}")
    private boolean cancelEndpointEnabled;
    @PutMapping("/{id}/cancel")
    public String cancelConsultation(@PathVariable(value = "id", required = false) Long id, @RequestParam(value = "from", required = false) String from, @RequestBody(required = false) String body) {
        if (!cancelEndpointEnabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Endpoint not found");
        }
        consultService.cancelConsultation(id, from, body);
        return "This consultation has been cancelled.";
    }

    @PutMapping("/cancel/{uniqueLink}")
    public String cancelConsultationUniqueLink(@PathVariable String uniqueLink) {
        consultService.cancelConsultationUniqueLink(uniqueLink);
        return "This consultation has been cancelled.";
    }


    /*
        @GetMapping
    public @ResponseBody List<ConsultResponse> getConsultByDate(@RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestParam(value = "sort", required = false) String sort, Principal principal) {
        User authUser = userRepository.findByUsername(principal.getName());
        List<Consultation> consultations = consultService.getConsultByDate(authUser, date, sort);
        return consultations.stream().map(ConsultResponse::new).collect(Collectors.toList());
    }

        @GetMapping("/allConsultations")
    public @ResponseBody List<ConsultResponse> getAllConsultations(Principal principal){
        User authUser = userRepository.findByUsername(principal.getName());
        List<ConsultResponse> allConsultations = consultService.getAllConsults(authUser);
        return allConsultations;
    }

    @GetMapping("/{status}")
    public @ResponseBody List<ConsultResponse> getConsultByStatus(@PathVariable("status")ConsultationStatus status, Principal principal){
        User authUser = userRepository.findByUsername(principal.getName());
        List<Consultation> consultations = consultService.getConsultByStatus(authUser, status);
        return consultations.stream().map(ConsultResponse::new).collect(Collectors.toList());
    }
     */

}
