package com.maidgroup.maidgroup.Service;

import com.maidgroup.maidgroup.dao.PasswordRepository;
import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.security.Password;
import com.maidgroup.maidgroup.security.PasswordEmbeddable;
import com.maidgroup.maidgroup.service.impl.UserServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;

import static com.maidgroup.maidgroup.model.userinfo.Gender.FEMALE;
import static org.mockito.Mockito.*;

public class UserServiceTestSuite {
    @InjectMocks
    UserServiceImpl sut;
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordRepository passwordRepository;
    @Mock
    BCryptPasswordEncoder passwordEncoder;

    @Before
    public void testPrep(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void registerUser_returnsNewUser_givenValidUser(){
        ///AAA Arrange Act Assert

        Password password = new Password("Missypfoo20!");

        //Arrange
        User newUser = new User();
        newUser.setUsername("missypfoo");
        newUser.setPassword(password);
        newUser.setConfirmPassword(password);
        newUser.setFirstName("missy");
        newUser.setLastName("foo");
        newUser.setEmail("missyfoo@cats.com");
        newUser.setGender(FEMALE);
        newUser.setDateOfBirth(LocalDate.of(2020, 9, 15));

        //Act
        when(userRepository.save(newUser)).thenReturn(newUser);
        User user = sut.register(newUser);

        //Assert
        verify(userRepository, times(1)).save(newUser);
        Assert.assertNotNull(user); //If user is not registered a null object is returned.
    }

    @Test
    public void loginUser_returnsSuccessfulLogin_givenExistingUser() {
        // Arrange
        String username = "missypfoo";
        String rawPassword = "Missypfoo20!";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        User existingUser = new User();
        existingUser.setUsername(username);
        existingUser.setPassword(new Password(hashedPassword));

        // Act
        when(userRepository.findByUsername(username)).thenReturn(existingUser);
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);
        User user = sut.login(username, rawPassword);

        // Assert
        verify(userRepository).findByUsername(username);
        Assert.assertNotNull(user);
    }
}
