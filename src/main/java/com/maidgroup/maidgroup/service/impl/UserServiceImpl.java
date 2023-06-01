package com.maidgroup.maidgroup.service.impl;

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
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    PasswordRepository passwordRepository;
    BCryptPasswordEncoder passwordEncoder;
    JWTUtility jwtUtility;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordRepository passwordRepository, BCryptPasswordEncoder passwordEncoder, JWTUtility jwtUtility) {
        this.userRepository = userRepository;
        this.passwordRepository = passwordRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtility = jwtUtility;
    }

    @Override
    public User login(String username, String password) {
        User u = userRepository.findByUsername(username);
        if(u != null && passwordEncoder.matches(password, u.getPassword().toString())){
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
    @Override
    public User register(User user) {
        String username = user.getUsername();
        User u = userRepository.findByUsername(username);
        if(u!=null) {
            throw new RuntimeException("Username is already taken");
        }
        if(StringUtils.isEmpty(user.getUsername())){
            throw new RuntimeException("Username must not be empty");
        }
        if(user.getFirstName().isEmpty()){
            throw new RuntimeException("First name cannot be empty");
        }
        if(user.getLastName().isEmpty()){
            throw new RuntimeException("Last name cannot be empty");
        }
        EmailValidator emailValidator = EmailValidator.getInstance();
        if (!emailValidator.isValid(user.getEmail())) {
            throw new RuntimeException("Invalid email address");
        }
        if(user.getGender() == null){
            throw new RuntimeException("Must select a gender option");
        }

        String password = user.getPassword().getHashedPassword();
        validatePassword(password);
        String confirmPassword = user.getConfirmPassword().getHashedPassword();

        if (!password.equals(confirmPassword)) {
            throw new PasswordMismatchException("Passwords do not match.");
        }
        if(user.getDateOfBirth() == null){
            throw new RuntimeException("Must enter your date of birth");
        }
        if(user.getDateOfBirth() != null && user.getDateOfBirth().isAfter(LocalDate.now())){
            throw new RuntimeException("Date of birth cannot be in the future.");
        }

        Password hashedPassword = new Password(passwordEncoder.encode(user.getPassword().toString()));
        PasswordEmbeddable hashedEmbeddablePassword = new PasswordEmbeddable(hashedPassword.toString());
        passwordRepository.save(hashedPassword);
        user.setPassword(new PasswordEmbeddable(hashedPassword.getHashedPassword(), hashedPassword.getDateLastUsed()));
        user.getPreviousPasswords().add(hashedEmbeddablePassword);

        userRepository.saveAndFlush(user);

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
                PasswordEmbeddable oldPassword = existingUser.getPassword();// get the old password from the existing user
                Optional<PasswordEmbeddable> oldPasswordEntity = existingUser.getPreviousPasswords().stream() // find the corresponding Password object in the previousPasswords list
                        .filter(password -> password.getHashedPassword().equals(oldPassword.getHashedPassword()))
                        .findFirst();
                oldPasswordEntity.ifPresent(password -> { // update the date last used of the old password
                    password.setDateLastUsed(LocalDate.now());
                });
                Password newPassword = new Password(passwordEncoder.encode(user.getPassword().toString()));// create a new Password object and save it to the database
                PasswordEmbeddable newPwdEmbeddable = new PasswordEmbeddable(newPassword.toString());
                passwordRepository.save(newPassword);
                existingUser.getPreviousPasswords().add(newPwdEmbeddable);// add the new Password object to the previousPasswords list
                existingUser.setPassword(new PasswordEmbeddable(newPassword.getHashedPassword(), newPassword.getDateLastUsed()));// set the password field to a new PasswordEmbeddable object created from the new Password object
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
}
