package com.maidgroup.maidgroup.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.service.UserService;
import com.maidgroup.maidgroup.service.exceptions.InvalidCredentialsException;
import com.maidgroup.maidgroup.service.exceptions.InvalidTokenException;
import com.maidgroup.maidgroup.service.exceptions.UnauthorizedException;
import com.maidgroup.maidgroup.service.exceptions.UserNotFoundException;
import com.maidgroup.maidgroup.util.dto.LoginCreds;
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
import java.util.List;
import java.util.Optional;

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

    @PostMapping("/login")
    public User login(@RequestBody LoginCreds loginCreds, HttpServletResponse response){
        User authUser = userService.login(loginCreds.getUsername(), loginCreds.getPassword());
        String token = jwtUtility.createToken(authUser);
        response.setHeader("Authorization", token);
        return authUser;
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

    @PostMapping("/registerUser")
    @PreAuthorize("permitAll")
    public ResponseEntity<?> register(@RequestBody String jsonPayload){
        try {
            // Parse the JSON payload to create a User object
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            User user = objectMapper.readValue(jsonPayload, User.class);

            // Call the register() method and pass the User object and JSON payload
            User registeredUser = userService.register(user, jsonPayload);

            return new ResponseEntity<User>(registeredUser, HttpStatus.CREATED);
        }catch (JsonProcessingException e){
        // Handle the exception
            log.error("Error parsing JSON payload", e);
            return new ResponseEntity<String>("Error parsing JSON payload", HttpStatus.BAD_REQUEST);
    }
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
    public @ResponseBody List<User> getAllUsers(Principal principal){
        User authUser = userRepository.findByUsername(principal.getName());
        return userService.getAllUsers(authUser);
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

