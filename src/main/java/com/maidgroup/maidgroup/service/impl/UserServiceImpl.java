package com.maidgroup.maidgroup.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maidgroup.maidgroup.dao.PasswordRepository;
import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.userinfo.Age;
import com.maidgroup.maidgroup.model.userinfo.Role;
import com.maidgroup.maidgroup.security.Password;
import com.maidgroup.maidgroup.security.PasswordEmbeddable;
import com.maidgroup.maidgroup.service.UserService;
import com.maidgroup.maidgroup.service.exceptions.*;
import com.maidgroup.maidgroup.util.tokens.JWTUtility;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.aspectj.bridge.MessageUtil.fail;

@Log4j2
@NoArgsConstructor
@Data
@Service
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    BCryptPasswordEncoder passwordEncoder;
    JWTUtility jwtUtility;
    //private Logger logger = LogManager.getLogger();

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JWTUtility jwtUtility) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtility = jwtUtility;
    }

    @Override
    public User login(String username, String password) {
        User u = userRepository.findByUsername(username)/*.orElseThrow(UserNotFoundException::new)*/;
        log.debug("Password being encoded: {}", password);
        log.debug("Encoded password: {}", u.getPassword().getHashedPassword());
        log.debug("Passed in password after being encoded: {}", passwordEncoder.encode(password));


        boolean matches = passwordEncoder.matches(password, u.getPassword().getHashedPassword());
        log.debug("passwordEncoder.matches() result: {}", matches);

        if(u != null && passwordEncoder.matches(password, u.getPassword().getHashedPassword())){
            return u;
        }else{
            throw new InvalidCredentialsException("The provided credentials were incorrect.");
        }
    }

    @Override
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if(session != null) {
            session.invalidate();
        }
    }

    @Override
    public User register(User user, String jsonPayload) {
        try {
            log.debug("JSON payload: {}", jsonPayload);
            String username = user.getUsername();
            User u = userRepository.findByUsername(username);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonPayload);
            String rawPassword = jsonNode.get("password").asText();
            user.setRawPassword(rawPassword);

            if (u != null) {
                throw new RuntimeException("Username is already taken");
            }
            if (StringUtils.isEmpty(user.getUsername())) {
                throw new RuntimeException("Username must not be empty");
            }
            if (user.getFirstName().isEmpty()) {
                throw new RuntimeException("First name cannot be empty");
            }
            if (user.getLastName().isEmpty()) {
                throw new RuntimeException("Last name cannot be empty");
            }
            EmailValidator emailValidator = EmailValidator.getInstance();
            if (!emailValidator.isValid(user.getEmail())) {
                throw new RuntimeException("Invalid email address");
            }
            if (user.getGender() == null) {
                throw new RuntimeException("Must select a gender option");
            }

            String password = user.getPassword().getHashedPassword();
            validatePassword(password);
            String confirmPassword = user.getConfirmPassword().getHashedPassword();

            if (!password.equals(confirmPassword)) {
                throw new PasswordMismatchException("Passwords do not match.");
            }
            if (user.getDateOfBirth() == null) {
                throw new RuntimeException("Must enter your date of birth");
            }
            if (user.getDateOfBirth() != null && user.getDateOfBirth().isAfter(LocalDate.now())) {
                throw new RuntimeException("Date of birth cannot be in the future.");
            }

            String hashedPassword = passwordEncoder.encode(user.getRawPassword());
            log.debug("Hashed password: {}", hashedPassword);
            Password hashedEmbeddablePassword = new Password(hashedPassword);
            user.setPassword(hashedEmbeddablePassword);
            user.getPreviousPasswords().add(hashedEmbeddablePassword);

        }catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON payload");
        }

        userRepository.save(user);

        Age age = new Age();
        user.setAge(age.getAge(user.getDateOfBirth()));

        return user;

    }

    @Override
    public User updateUser(User user) {
        Optional<User> optionalUser = userRepository.findById(user.getUserId());
        if(optionalUser.isPresent()){
            User existingUser = optionalUser.get();
            boolean isAdmin = user.getRole().equals(Role.Admin);
            boolean isMatching = user.getUserId() == existingUser.getUserId();

            if(!isAdmin){
                if(!isMatching) {
                    throw new UnauthorizedException("You are not authorized to update this account.");
                }
            }
            if(user.getUsername() != null && !user.getUsername().equals(existingUser.getUsername())){
                if(userRepository.findByUsername(user.getUsername())!=null){
                    throw new UsernameAlreadyExists("Username is already taken.");
                }
                existingUser.setUsername(user.getUsername());
            }

            if(user.getPassword() != null && !user.getPassword().equals(existingUser.getPassword())){

                validatePassword(user.getPassword().toString());

                if(user.getConfirmPassword() == null || user.getConfirmPassword().toString().trim().equals("") || !user.getConfirmPassword().toString().equals(user.getPassword().toString())){
                    throw new PasswordMismatchException("Passwords do not match.");
                }
                if(existingUser.getPreviousPasswords().stream().anyMatch(p -> passwordEncoder.matches(user.getPassword().toString(), String.valueOf(p)))){
                    throw new InvalidPasswordException("Password has already been used.");
                }
                Password oldPassword = existingUser.getPassword();// get the old password from the existing user
                Optional<Password> oldPasswordEntity = existingUser.getPreviousPasswords().stream() // find the corresponding Password object in the previousPasswords list
                        .filter(password -> password.getHashedPassword().equals(oldPassword.getHashedPassword()))
                        .findFirst();
                oldPasswordEntity.ifPresent(password -> { // update the date last used of the old password
                    password.setDateLastUsed(LocalDate.now());
                });
                Password newPassword = new Password(passwordEncoder.encode(user.getPassword().toString()));// create a new Password object and save it to the database
                Password newPwdEmbeddable = new Password(newPassword.toString());
                existingUser.getPreviousPasswords().add(newPwdEmbeddable);// add the new Password object to the previousPasswords list
                existingUser.setPassword(new Password(newPassword.getHashedPassword(), newPassword.getDateLastUsed()));// set the password field to a new PasswordEmbeddable object created from the new Password object

                userRepository.save(existingUser);

            }
            if(user.getFirstName() != null){
                existingUser.setFirstName(user.getFirstName());
            }
            if(user.getLastName() != null){
                existingUser.setLastName(user.getLastName());
            }
            if(user.getEmail() != null){
                EmailValidator emailValidator = EmailValidator.getInstance();
                if (!emailValidator.isValid(user.getEmail())) {
                    throw new RuntimeException("Invalid email address");
                }
                existingUser.setEmail(user.getEmail());
            }
            if(user.getGender() != null){
                existingUser.setGender(user.getGender());
            }
            if(user.getDateOfBirth() != null){
                if(user.getDateOfBirth().isAfter(LocalDate.now())){
                    throw new RuntimeException("Date of birth cannot be in the future.");
                }
                Age age = new Age();
                existingUser.setDateOfBirth(user.getDateOfBirth());
                existingUser.setAge(age.getAge(user.getDateOfBirth()));
            }

            return userRepository.save(existingUser);
        }else{
            throw new UserNotFoundException("No user was found.");
        }
    }

    @Override
    public List<User> getAllUsers(User user) {
        List<User> allUsers = userRepository.findAll();
        if(!user.getRole().equals(Role.Admin)){
            throw new UnauthorizedException("You are not authorized to view all accounts.");
        }
        if(allUsers.isEmpty()) {
            throw new UserNotFoundException("No users were found.");
        }
        return allUsers;
    }

    @Override
    public User getByUsername(String username, User requester) {
        Optional<User> user = userRepository.findById(requester.getUserId());
        boolean isAdmin = requester.getRole().equals(Role.Admin);
        if(user.isPresent()){
            if (isAdmin) {
                return user.get();
            } else if (requester.getUsername().equals(username)){
                return user.get();
            } else {
                throw new UnauthorizedException("You are not authorized to retrieve this account's information.");
            }
        }else{
            throw new UserNotFoundException("There is no existing account with the username "+username+".");
        }
    }

    @Override
    public void delete(String username, User requester) {
        User userToDelete = userRepository.findByUsername(username);
        boolean isAdmin = requester.getRole().equals(Role.Admin);
        if (isAdmin || requester.getUsername().equals(userToDelete.getUsername())) {
            userRepository.delete(userToDelete);
        } else {
            throw new UnauthorizedException("You are not authorized to delete this account.");
        }
    }

    private void validatePassword(String password) {
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}$";
        String[] requirements = {
                "At least 8 characters long",
                "Contains at least one digit",
                "Contains at least one lowercase letter",
                "Contains at least one uppercase letter",
                "Contains at least one special character (!@#$%^&+=)",
                "Cannot have empty spaces"
        };
        List<String> missingRequirements = new ArrayList<>();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        if (!matcher.find()) {
            for (int i = 0; i < requirements.length; i++) {
                if (!password.matches(".*[" + getRegexForRequirement(i) + "]")) {
                    missingRequirements.add(requirements[i]);
                }
            }
            throw new InvalidPasswordException("Password does not meet the following requirements: " + String.join(", ", missingRequirements));
        }
    }

    private String getRegexForRequirement(int index) {
        switch (index) {
            case 0:
                return ".";
            case 1:
                return "0-9";
            case 2:
                return "a-z";
            case 3:
                return "A-Z";
            case 4:
                return "!@#$%^&+=";
            case 5:
                return " ";
            default:
                return "";
        }
    }
}
