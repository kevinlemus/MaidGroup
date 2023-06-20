package com.maidgroup.maidgroup.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.security.Password;
import com.maidgroup.maidgroup.service.UserService;
import com.maidgroup.maidgroup.service.exceptions.InvalidCredentialsException;
import com.maidgroup.maidgroup.service.exceptions.InvalidTokenException;
import com.maidgroup.maidgroup.service.exceptions.UnauthorizedException;
import com.maidgroup.maidgroup.service.exceptions.UserNotFoundException;
import com.maidgroup.maidgroup.util.dto.LoginCreds;
import com.maidgroup.maidgroup.util.dto.Requests.UserRequest;
import com.maidgroup.maidgroup.util.dto.Responses.UserResponse;
import com.maidgroup.maidgroup.util.tokens.JWTUtility;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@CrossOrigin
@Log4j2
public class UserController {
    UserService userService;
    UserRepository userRepository;
    JWTUtility jwtUtility;
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository, JWTUtility jwtUtility, BCryptPasswordEncoder passwordEncoder){
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtUtility = jwtUtility;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/registerUser")
    @PreAuthorize("permitAll")
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest userRequest) {
        // Create a User object from the UserRequest object
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setPassword(new Password(userRequest.getPassword()));
        user.setRawPassword(userRequest.getPassword());
        user.setConfirmPassword(new Password(userRequest.getConfirmPassword()));
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setGender(userRequest.getGender());
        user.setDateOfBirth(userRequest.getDateOfBirth());
        user.setRole(userRequest.getRole());

        // Call the register() method and pass the User object
        User registeredUser = userService.register(user);

        // Create a UserResponse object from the registeredUser object
        UserResponse userResponse = new UserResponse(registeredUser);

        return new ResponseEntity<UserResponse>(userResponse, HttpStatus.CREATED);
    }


    @ExceptionHandler({JsonProcessingException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody String exceptionInvalidJsonProcessing(JsonProcessingException e){
        return e.getMessage();
    }
    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginCreds loginCreds, HttpServletResponse response){
        User authUser = userService.login(loginCreds.getUsername(), loginCreds.getPassword());
        UserResponse userResponse = new UserResponse(authUser);
        String token = jwtUtility.createToken(authUser);
        response.setHeader("Authorization", token);

        return userResponse;
    }

    @ExceptionHandler({InvalidCredentialsException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody String exceptionInvalidUserInput(InvalidCredentialsException e){
        return e.getMessage();
    }

  /*  public ResponseEntity<User> login(@RequestBody User loginRequest, HttpServletRequest request){
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword().toString();
        boolean loggedIn = userService.login(username, password, request);

        if(loggedIn){
            User user = userRepository.findByUsername(username);
            String message = "You have successfully logged in";
            return ResponseEntity.ok()
                    .header("loggedIn", "Success")
                    .header("message", message)
                    .body(user);

        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }*/

    @PostMapping("/logout")
    public ResponseEntity<User> logout(@RequestBody User logoutRequest, HttpServletRequest request){
        userService.logout(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{username}")
    public ResponseEntity<String> delete(@PathVariable("username") String username, @RequestBody User requester) {
        try {
            userService.delete(username, requester);
            return new ResponseEntity<String>("Your account has been deleted", HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedException d) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> getByUsername(@PathVariable("username") String username, @RequestBody User requester){
        try {
            return new ResponseEntity<User>(userService.getByUsername(username, requester), HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedException d) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/getAllUsers")
    public @ResponseBody List<UserResponse> getAllUsers(Principal principal){
        User authUser = userRepository.findByUsername(principal.getName());
        List<UserResponse> allUsers = userService.getAllUsers(authUser);
        return allUsers;
    }

    /*
    public ResponseEntity<List<User>> getAllUsers(@RequestBody User user){
        List<User> allUsers = userService.getAllUsers(user);
        return ResponseEntity.status(HttpStatus.OK).body(allUsers);
    }*/

    @PutMapping("/{username}")
    public ResponseEntity<User> updateUser(@RequestBody User user){
        return new ResponseEntity<User>(userService.updateUser(user), HttpStatus.OK);
    }
}

