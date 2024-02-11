package com.maidgroup.maidgroup.service;

import com.maidgroup.maidgroup.dao.ConsultationRepository;
import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.Consultation;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.consultationinfo.ConsultationStatus;
import com.maidgroup.maidgroup.model.consultationinfo.PreferredContact;
import com.maidgroup.maidgroup.model.userinfo.Role;
import com.maidgroup.maidgroup.service.exceptions.ConsultationNotFoundException;
import com.maidgroup.maidgroup.service.exceptions.InvalidSmsMessageException;
import com.maidgroup.maidgroup.service.exceptions.UnauthorizedException;
import com.maidgroup.maidgroup.service.exceptions.UserNotFoundException;
import com.maidgroup.maidgroup.service.impl.ConsultationServiceImpl;
import com.maidgroup.maidgroup.util.twilio.TwilioSMS;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ConsultServiceTestSuite {

    @InjectMocks
    ConsultationServiceImpl sut;

    @Mock
    ConsultationRepository consultRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    EmailService emailService;  // Mock EmailService

    @Mock
    TwilioSMS twilioSMS;

    @Before
    public void testPrep(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_createConsultation_returnsNewConsultation_givenValidConsultation() {
        // Arrange
        Consultation newConsult = new Consultation();
        newConsult.setFirstName("Test");
        newConsult.setLastName("User");
        newConsult.setEmail("testuser@example.com");
        newConsult.setPhoneNumber("+12025551234"); // valid US phone number
        newConsult.setMessage("This is a test message");
        newConsult.setDate(LocalDate.now());
        newConsult.setTime(LocalTime.now());
        newConsult.setPreferredContact(PreferredContact.Email);
        newConsult.setStatus(ConsultationStatus.OPEN);
        newConsult.setUniqueLink("uniqueLink");

        when(consultRepository.findById(any(Long.class))).thenReturn(Optional.empty());
        when(consultRepository.save(any(Consultation.class))).thenReturn(newConsult);

        // Mock the methods of TwilioSMS and EmailService
        doNothing().when(twilioSMS).sendSMS(anyString(), anyString());
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // Act
        Consultation consultation = sut.create(newConsult);

        // Assert
        assertNotNull(consultation);
        assertEquals(newConsult, consultation);
    }

    @Test
    public void test_getConsultById_returnsConsultation_givenValidId() {
        // Arrange
        Consultation expectedConsult = new Consultation();
        expectedConsult.setFirstName("Test");
        expectedConsult.setLastName("User");
        expectedConsult.setEmail("testuser@example.com");
        expectedConsult.setPhoneNumber("+12025551234"); // valid US phone number
        expectedConsult.setMessage("This is a test message");
        expectedConsult.setDate(LocalDate.now());
        expectedConsult.setTime(LocalTime.now());
        expectedConsult.setPreferredContact(PreferredContact.Email);
        expectedConsult.setStatus(ConsultationStatus.OPEN);
        expectedConsult.setUniqueLink("uniqueLink");

        when(consultRepository.findById(any(Long.class))).thenReturn(Optional.of(expectedConsult));

        // Act
        Consultation actualConsult = sut.getConsultById(1L);

        // Assert
        assertNotNull(actualConsult);
        assertEquals(expectedConsult, actualConsult);
    }

    @Test(expected = ConsultationNotFoundException.class)
    public void test_getConsultById_throwsConsultationNotFoundException_givenInvalidId() {
        // Arrange
        when(consultRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // Act
        sut.getConsultById(1L);
    }

    @Test
    public void test_getConsults_returnsConsultations_givenValidParameters() {
        // Arrange
        User requester = new User();
        requester.setUserId(1L);
        requester.setRole(Role.ADMIN);

        Consultation consultation1 = new Consultation();
        consultation1.setDate(LocalDate.now());
        consultation1.setStatus(ConsultationStatus.OPEN);
        consultation1.setPreferredContact(PreferredContact.Email);
        consultation1.setFirstName("Test");
        consultation1.setLastName("User");

        Consultation consultation2 = new Consultation();
        consultation2.setDate(LocalDate.now().minusDays(1));
        consultation2.setStatus(ConsultationStatus.CLOSED);
        consultation2.setPreferredContact(PreferredContact.Call);
        consultation2.setFirstName("Another");
        consultation2.setLastName("User");

        List<Consultation> expectedConsultations = Arrays.asList(consultation1, consultation2);

        when(consultRepository.findAll()).thenReturn(expectedConsultations);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(requester));

        // Act
        List<Consultation> actualConsultations = sut.getConsults(requester, LocalDate.now(), ConsultationStatus.OPEN, PreferredContact.Email, "Test", "recent");

        // Assert
        assertNotNull(actualConsultations);
        assertEquals(1, actualConsultations.size());
        assertEquals(consultation1, actualConsultations.get(0));
    }

    @Test(expected = UnauthorizedException.class)
    public void test_getConsults_throwsUnauthorizedException_givenNonAdminUser() {
        // Arrange
        User requester = new User();
        requester.setUserId(1L);
        requester.setRole(Role.USER);

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(requester));

        // Act
        sut.getConsults(requester, null, null, null, null, null);
    }

    @Test(expected = UserNotFoundException.class)
    public void test_getConsults_throwsUserNotFoundException_givenInvalidUserId() {
        // Arrange
        User requester = new User();
        requester.setUserId(1L);
        requester.setRole(Role.ADMIN);

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // Act
        sut.getConsults(requester, null, null, null, null, null);
    }

    @Test
    public void test_delete_deletesConsultation_givenValidIdAndAdminUser() {
        // Arrange
        User requester = new User();
        requester.setUserId(1L);
        requester.setRole(Role.ADMIN);

        Consultation consultation = new Consultation();
        consultation.setId(1L);

        when(consultRepository.findById(any(Long.class))).thenReturn(Optional.of(consultation));

        // Act
        sut.delete(1L, requester);

        // Assert
        verify(consultRepository, times(1)).delete(consultation);
    }

    @Test(expected = UnauthorizedException.class)
    public void test_delete_throwsUnauthorizedException_givenNonAdminUser() {
        // Arrange
        User requester = new User();
        requester.setUserId(1L);
        requester.setRole(Role.USER);

        Consultation consultation = new Consultation();
        consultation.setId(1L);

        when(consultRepository.findById(any(Long.class))).thenReturn(Optional.of(consultation));

        // Act
        sut.delete(1L, requester);
    }

    @Test(expected = ConsultationNotFoundException.class)
    public void test_delete_throwsConsultationNotFoundException_givenInvalidId() {
        // Arrange
        User requester = new User();
        requester.setUserId(1L);
        requester.setRole(Role.ADMIN);

        when(consultRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // Act
        sut.delete(1L, requester);
    }

    @Test
    public void test_deleteConsultations_deletesConsultations_givenValidIdsAndAdminUser() {
        // Arrange
        User requester = new User();
        requester.setUserId(1L);
        requester.setRole(Role.ADMIN);

        List<Long> ids = Arrays.asList(1L, 2L);

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(requester));

        // Act
        sut.deleteConsultations(requester, ids);

        // Assert
        verify(consultRepository, times(1)).deleteAllById(ids);
    }

    @Test(expected = UnauthorizedException.class)
    public void test_deleteConsultations_throwsUnauthorizedException_givenNonAdminUser() {
        // Arrange
        User requester = new User();
        requester.setUserId(1L);
        requester.setRole(Role.USER);

        List<Long> ids = Arrays.asList(1L, 2L);

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(requester));

        // Act
        sut.deleteConsultations(requester, ids);
    }

    @Test(expected = UserNotFoundException.class)
    public void test_deleteConsultations_throwsUserNotFoundException_givenInvalidUserId() {
        // Arrange
        User requester = new User();
        requester.setUserId(1L);
        requester.setRole(Role.ADMIN);

        List<Long> ids = Arrays.asList(1L, 2L);

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // Act
        sut.deleteConsultations(requester, ids);
    }

    @Test
    public void test_cancelConsultation_cancelsConsultation_givenValidId() {
        // Arrange
        Consultation consultation = new Consultation();
        consultation.setId(1L);

        when(consultRepository.findById(any(Long.class))).thenReturn(Optional.of(consultation));
        doNothing().when(twilioSMS).sendSMS(anyString(), anyString());
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // Act
        sut.cancelConsultation(1L, null, null);

        // Assert
        assertEquals(ConsultationStatus.CANCELLED, consultation.getStatus());
        verify(consultRepository, times(1)).save(consultation);
    }

    @Test(expected = ConsultationNotFoundException.class)
    public void test_cancelConsultation_throwsConsultationNotFoundException_givenInvalidId() {
        // Arrange
        when(consultRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // Act
        sut.cancelConsultation(1L, null, null);
    }

    @Test
    public void test_cancelConsultation_cancelsConsultation_givenValidPhoneNumberAndBody() {
        // Arrange
        Consultation consultation = new Consultation();
        consultation.setId(1L);
        consultation.setPhoneNumber("+12025551234");

        when(consultRepository.findByPhoneNumber(anyString())).thenReturn(Optional.of(consultation));
        doNothing().when(twilioSMS).sendSMS(anyString(), anyString());
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // Act
        sut.cancelConsultation(null, "+12025551234", "CANCEL");

        // Assert
        assertEquals(ConsultationStatus.CANCELLED, consultation.getStatus());
        verify(consultRepository, times(1)).save(consultation);
    }

    @Test(expected = InvalidSmsMessageException.class)
    public void test_cancelConsultation_throwsInvalidSmsMessageException_givenInvalidBody() {
        // Arrange
        Consultation consultation = new Consultation();
        consultation.setId(1L);
        consultation.setPhoneNumber("+12025551234");

        when(consultRepository.findByPhoneNumber(anyString())).thenReturn(Optional.of(consultation));

        // Act
        sut.cancelConsultation(null, "+12025551234", "INVALID");
    }

    @Test(expected = ConsultationNotFoundException.class)
    public void test_cancelConsultation_throwsConsultationNotFoundException_givenInvalidPhoneNumber() {
        // Arrange
        when(consultRepository.findByPhoneNumber(anyString())).thenReturn(Optional.empty());

        // Act
        sut.cancelConsultation(null, "+12025551234", "CANCEL");
    }

    @Test
    public void test_cancelConsultationUniqueLink_cancelsConsultation_givenValidUniqueLink() {
        // Arrange
        Consultation consultation = new Consultation();
        consultation.setUniqueLink("uniqueLink");

        when(consultRepository.findByUniqueLink(anyString())).thenReturn(Optional.of(consultation));
        doNothing().when(twilioSMS).sendSMS(anyString(), anyString());
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // Act
        sut.cancelConsultationUniqueLink("uniqueLink");

        // Assert
        assertEquals(ConsultationStatus.CANCELLED, consultation.getStatus());
        verify(consultRepository, times(1)).save(consultation);
    }

    @Test(expected = ConsultationNotFoundException.class)
    public void test_cancelConsultationUniqueLink_throwsConsultationNotFoundException_givenInvalidUniqueLink() {
        // Arrange
        when(consultRepository.findByUniqueLink(anyString())).thenReturn(Optional.empty());

        // Act
        sut.cancelConsultationUniqueLink("invalidLink");
    }

    @Test
    public void test_generateUniqueLink_returnsUniqueLink() {
        // Act
        String uniqueLink1 = sut.generateUniqueLink();
        String uniqueLink2 = sut.generateUniqueLink();

        // Assert
        assertNotNull(uniqueLink1);
        assertFalse(uniqueLink1.isEmpty());
        assertNotNull(uniqueLink2);
        assertFalse(uniqueLink2.isEmpty());
        assertNotEquals(uniqueLink1, uniqueLink2);
    }


}

