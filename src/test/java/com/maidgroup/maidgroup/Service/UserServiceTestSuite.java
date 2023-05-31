package com.maidgroup.maidgroup.Service;

import com.maidgroup.maidgroup.dao.PasswordRepository;
import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.security.Password;
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

import static com.maidgroup.maidgroup.model.userinfo.Gender.Female;

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
        newUser.setGender(Female);
        newUser.setDateOfBirth(LocalDate.of(2020, 9, 15));

        //Act
        User user = sut.register(newUser);

        //Assert
        Assert.assertNotNull(user); //If user is not registered a null object is returned.
    }

    @Test
    public void loginUser_returnsSuccessfulLogin_givenExistingUser(){

        Password password = new Password("Missypfoo20!");
        //Arrange
        User existingUser = new User();
        existingUser.setUsername("missypfoo");
        existingUser.setPassword(password);

        //Act
        User user = sut.login(existingUser.getUsername(), existingUser.getPassword().toString());

        //Assert
        Assert.assertNotNull(user);
    }
}
