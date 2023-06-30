package com.maidgroup.maidgroup.Service;

import com.maidgroup.maidgroup.dao.PasswordRepository;
import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.userinfo.Role;
import com.maidgroup.maidgroup.security.Password;
import com.maidgroup.maidgroup.security.PasswordEmbeddable;
import com.maidgroup.maidgroup.service.impl.UserServiceImpl;
import com.maidgroup.maidgroup.util.tokens.JWTUtility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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
    // Create a mock instance of the JwtUtility class
    @Mock
    JWTUtility jwtUtility;

    @Before
    public void testPrep() {
        MockitoAnnotations.initMocks(this);
        // Set the value of the jwtUtility field on the UserServiceImpl object
        ReflectionTestUtils.setField(sut, "jwtUtility", jwtUtility);
    }

    @Test
    public void registerUser_returnsNewUser_givenValidUser() {
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

    @Test
    public void updateUser_returnsSuccessfulUpdate_givenExistingUserUpdates() {
        //Arrange
        String username = "missypfoo";
        String rawPassword = "Missypfoo20!";
        Password password = new Password(passwordEncoder.encode(rawPassword));

        User existingUser = new User();
        existingUser.setUsername(username);
        // Set a value for the password field
        existingUser.setPassword(password);
        existingUser.setRawPassword(rawPassword);
        existingUser.setConfirmPassword(password);
        existingUser.setFirstName("missy");
        existingUser.setLastName("foo");
        existingUser.setEmail("missyfoo@cats.com");
        existingUser.setGender(FEMALE);
        existingUser.setDateOfBirth(LocalDate.of(2020, 9, 15));
        // Set a value for the role field
        existingUser.setRole(Role.USER);

        String newRawPassword = "WhassupDawgs80!";
        Password newPassword = new Password(passwordEncoder.encode(newRawPassword));

        User updatedUser = new User();
        updatedUser.setUsername(username);
        // Set a different value for the password field
        updatedUser.setPassword(newPassword);
        updatedUser.setRawPassword(newRawPassword);
        updatedUser.setConfirmPassword(newPassword);
        updatedUser.setFirstName("newMissy");
        updatedUser.setLastName("newFoo");
        updatedUser.setGender(FEMALE);
        updatedUser.setDateOfBirth(LocalDate.of(2020, 9, 15));

        //Act
        when(userRepository.findByUsername(username)).thenReturn(existingUser);
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        User user = sut.updateUser(updatedUser);

        //Assert
        verify(userRepository).findByUsername(username);
        verify(userRepository).save(existingUser);
        Assert.assertEquals(updatedUser.getFirstName(), user.getFirstName());
    }

}
