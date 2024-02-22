package com.maidgroup.maidgroup.service.impl;

import com.maidgroup.maidgroup.dao.PasswordRepository;
import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.userinfo.Age;
import com.maidgroup.maidgroup.model.userinfo.Gender;
import com.maidgroup.maidgroup.model.userinfo.Role;
import com.maidgroup.maidgroup.security.Password;
import com.maidgroup.maidgroup.service.EmailService;
import com.maidgroup.maidgroup.service.UserService;
import com.maidgroup.maidgroup.service.exceptions.*;
import com.maidgroup.maidgroup.util.dto.Requests.ForgotPasswordRequest;
import com.maidgroup.maidgroup.util.dto.Requests.ResetPasswordRequest;
import com.maidgroup.maidgroup.util.dto.Responses.UserResponse;
import com.maidgroup.maidgroup.util.tokens.JWTUtility;
import io.micrometer.common.util.StringUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Transactional
@Log4j2
@NoArgsConstructor
@Data
@Service
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    BCryptPasswordEncoder passwordEncoder;
    JWTUtility jwtUtility;
    //private Logger logger = LogManager.getLogger();
    EmailService emailService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JWTUtility jwtUtility, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtility = jwtUtility;
        this.emailService = emailService;
    }

    @Transactional
    @Override
    public void deactivateAccount(Long userId, User requester) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No user was found."));

        boolean isAdmin = requester.getRole().equals(Role.ADMIN);
        if (isAdmin || requester.getUserId().equals(user.getUserId())) {
            user.setDeactivationDate(LocalDate.now());
            userRepository.save(user);
        } else {
            throw new UnauthorizedException("You are not authorized to deactivate this account.");
        }
    }

    @Override
    public User login(String username, String password) {
        User u = userRepository.findByUsername(username);
        if (u == null) {
            throw new UserNotFoundException("User not found.");
        }

        boolean matches = passwordEncoder.matches(password, u.getPassword().getHashedPassword());

        if (u != null && matches) {
            // If the user is deactivated, reactivate the account
            if (u.getDeactivationDate() != null) {
                u.setDeactivationDate(null);
                userRepository.save(u);
            }
            return u;
        } else {
            throw new InvalidCredentialsException("The provided credentials were incorrect.");
        }
    }

    private Set<String> blacklist = new HashSet<>();

    @Override
    public void logout(String token) {
        if (isTokenValid(token)) {
            blacklist.add(token);
        }else {
            throw new InvalidCredentialsException("Invalid Token");
        }
    }

    public boolean isTokenValid(String token){
        return !blacklist.contains(token);
    }

    @Transactional
    @Override
    public User register(User user) {
        String username = user.getUsername();
        String email = user.getEmail();
        User u = userRepository.findByUsername(username);
        User e = userRepository.findByEmailOrUsername(email);

        if (u != null) {
            throw new RuntimeException("Username is already taken");
        }
        if (StringUtils.isEmpty(user.getUsername())) {
            throw new RuntimeException("Username must not be empty");
        }
        if (user.getFirstName() != null && user.getFirstName().isEmpty()) {
            throw new RuntimeException("First name cannot be empty");
        }
        if (user.getLastName() != null && user.getLastName().isEmpty()) {
            throw new RuntimeException("Last name cannot be empty");
        }
        EmailValidator emailValidator = EmailValidator.getInstance();
        if (!emailValidator.isValid(user.getEmail())) {
            throw new RuntimeException("Invalid email address");
        }
        if (e != null) {
            throw new RuntimeException("Email is already taken");
        }
        if (user.getGender() == null) {
            throw new RuntimeException("Must select a gender option");
        }

        String rawPassword = user.getRawPassword();
        log.debug("Raw password: {}", rawPassword);
        validatePassword(rawPassword);
        String confirmPassword = user.getConfirmPassword().getHashedPassword();

        if (!rawPassword.equals(confirmPassword)) {
            throw new PasswordMismatchException("Passwords do not match.");
        }
        if (user.getDateOfBirth() == null) {
            throw new RuntimeException("Must enter your date of birth");
        }
        if (user.getDateOfBirth() != null && user.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new RuntimeException("Date of birth cannot be in the future.");
        }

        String hashedPassword = passwordEncoder.encode(rawPassword);
        log.debug("Hashed password: {}", hashedPassword);
        Password hashedEmbeddablePassword = new Password(hashedPassword);
        user.setPassword(hashedEmbeddablePassword);
        user.getPreviousPasswords().add(hashedEmbeddablePassword);

        Age age = new Age();
        user.setAge(age.getAge(user.getDateOfBirth()));

        userRepository.save(user);

        return user;

    }

    @Transactional
    @Override
    public User updateUser(User user) {
        Optional<User> optionalUser = userRepository.findById(user.getUserId());
        if(optionalUser.isPresent()){
            User existingUser = optionalUser.get();
            boolean isAdmin = existingUser.getRole().equals(Role.ADMIN);
            boolean isMatching = user.getUserId().equals(existingUser.getUserId());

            if(!isAdmin){
                if(!isMatching) {
                    throw new UnauthorizedException("You are not authorized to update this account.");
                }
            } else {
                // Allow ADMIN users to update other user's role
                existingUser.setRole(user.getRole());
            }
            if(user.getUsername() != null){
                if(user.getUsername().equals(existingUser.getUsername())){
                    throw new UsernameAlreadyExists("Username cannot be the same.");
                }
                if(userRepository.findByUsername(user.getUsername()) != null){
                    throw new UsernameAlreadyExists("Username is already taken.");
                }
                existingUser.setUsername(user.getUsername());
            }

            if(user.getPassword() != null){
                if (user.getPassword().equals(existingUser.getPassword())){
                    throw new InvalidPasswordException("Password must be different.");
                }

                if(user.getRawPassword() != null){
                    validatePassword(user.getRawPassword());
                    String rawPassword = user.getRawPassword();

                    if(user.getConfirmPassword() != null){
                        String confirmPassword = user.getConfirmPassword().getHashedPassword();

                        if(!rawPassword.equals(confirmPassword)){
                            throw new PasswordMismatchException("Passwords do not match.");
                        }
                    }

                    if(existingUser.getPreviousPasswords().stream().anyMatch(p -> passwordEncoder.matches(rawPassword, p.getHashedPassword()))){
                        throw new InvalidPasswordException("Password has already been used.");
                    }
                    Password oldPassword = existingUser.getPassword();// get the old password from the existing user
                    oldPassword.setDateLastUsed(LocalDate.now());
                    existingUser.getPreviousPasswords().add(oldPassword);
                    Password newPassword = new Password(passwordEncoder.encode(rawPassword));// create a new Password object and save it to the database
                    existingUser.getPreviousPasswords().add(newPassword);// add the new Password object to the previousPasswords list
                    existingUser.setPassword(new Password(newPassword.getHashedPassword(), newPassword.getDateLastUsed()));// set the password field to a new PasswordEmbeddable object created from the new Password object
                }
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
                    throw new InvalidDateException("Date of birth cannot be in the future.");
                }
                Age age = new Age();
                existingUser.setDateOfBirth(user.getDateOfBirth());
                existingUser.setAge(age.getAge(user.getDateOfBirth()));
            }

            // Save the user after all updates have been made
            return userRepository.save(existingUser);
        } else {
            throw new UserNotFoundException("No user was found.");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<User> getAllUsers(User requester, String sort, String firstName, String lastName, Gender gender, String email) {
        if(!requester.getRole().equals(Role.ADMIN)){
            throw new UnauthorizedException("You are not authorized to view all accounts.");
        }
        List<User> allUsers = userRepository.findAll();
        if(allUsers.isEmpty()) {
            throw new UserNotFoundException("No users were found.");
        }

        // Filter by first name
        if (firstName != null) {
            allUsers = allUsers.stream()
                    .filter(u -> u.getFirstName().equalsIgnoreCase(firstName))
                    .collect(Collectors.toList());
        }

        // Filter by last name
        if (lastName != null) {
            allUsers = allUsers.stream()
                    .filter(u -> u.getLastName().equalsIgnoreCase(lastName))
                    .collect(Collectors.toList());
        }

        // Filter by email
        if (email != null) {
            allUsers = allUsers.stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(email))
                    .collect(Collectors.toList());
        }

        // Filter by gender
        if (gender != null) {
            allUsers = allUsers.stream()
                    .filter(u -> u.getGender().equals(gender))
                    .collect(Collectors.toList());
        }

        // Sort
        if (sort != null) {
            switch (sort) {
                case "usernameAsc":
                    allUsers.sort(Comparator.comparing(User::getUsername));
                    break;
                case "usernameDesc":
                    allUsers.sort(Comparator.comparing(User::getUsername).reversed());
                    break;
                case "firstNameAsc":
                    allUsers.sort(Comparator.comparing(User::getFirstName));
                    break;
                case "firstNameDesc":
                    allUsers.sort(Comparator.comparing(User::getFirstName).reversed());
                    break;
                case "lastNameAsc":
                    allUsers.sort(Comparator.comparing(User::getLastName));
                    break;
                case "lastNameDesc":
                    allUsers.sort(Comparator.comparing(User::getLastName).reversed());
                    break;
                case "emailAsc":
                    allUsers.sort(Comparator.comparing(User::getEmail));
                    break;
                case "emailDesc":
                    allUsers.sort(Comparator.comparing(User::getEmail).reversed());
                    break;
                case "ageAsc":
                    allUsers.sort(Comparator.comparing(User::getAge));
                    break;
                case "ageDesc":
                    allUsers.sort(Comparator.comparing(User::getAge).reversed());
                    break;
                case "genderAsc":
                    allUsers.sort(Comparator.comparing(User::getGender));
                    break;
                case "genderDesc":
                    allUsers.sort(Comparator.comparing(User::getGender).reversed());
                    break;
                // Add more cases as needed for other fields
            }
        }

        return allUsers;
    }


    @Transactional(readOnly = true)
    @Override
    public User getByUsername(String username, User requester) {
        Optional<User> user = Optional.ofNullable(userRepository.findByUsername(username));
        boolean isAdmin = requester.getRole().equals(Role.ADMIN);
        if(user.isPresent()){
            if (isAdmin || requester.getUsername().equals(username)) {
                return user.get();
            } else {
                throw new UnauthorizedException("You are not authorized to retrieve this account's information.");
            }
        }else{
            throw new UserNotFoundException("There is no existing account with the username "+username+".");
        }
    }

    @Transactional
    @Override
    public void delete(Long userId, User requester) {
        Optional<User> userToDelete = userRepository.findById(userId);
        if (userToDelete.isPresent()) {
            boolean isAdmin = requester.getRole().equals(Role.ADMIN);
            if (isAdmin || requester.getUserId().equals(userToDelete.get().getUserId())) {
                userRepository.delete(userToDelete.get());
            } else {
                throw new UnauthorizedException("You are not authorized to delete this account.");
            }
        } else {
            throw new UserNotFoundException("No user was found.");
        }
    }
    @Transactional
    @Override
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        User user = userRepository.findByEmailOrUsername(forgotPasswordRequest.getEmailOrUsername());
        if (user == null) {
            throw new UserNotFoundException("No user found with this email or username: " + forgotPasswordRequest.getEmailOrUsername());
        }

        String token = UUID.randomUUID().toString();
        Password password = user.getPassword();
        password.setResetToken(token);
        userRepository.save(user);

        // Here, you should implement the logic to send the email.
        String resetLink = "https://website.com/resetPassword?token=" + token;
        String message = "To reset your password, click the following link: " + resetLink;
        emailService.sendEmail(user.getEmail(), "Password Reset", message);
    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        User user = userRepository.findByPassword_ResetToken(resetPasswordRequest.getToken());
        if (user == null) {
            throw new InvalidTokenException("Invalid reset token");
        }

        String encodedPassword = passwordEncoder.encode(resetPasswordRequest.getNewPassword());
        user.setPassword(new Password(encodedPassword));
        userRepository.save(user);

        // Invalidate the token after it has been used
        user.getPassword().setResetToken(null);
        userRepository.save(user);
    }

    void validatePassword(String password) {
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
