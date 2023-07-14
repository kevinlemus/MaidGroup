package com.maidgroup.maidgroup.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.userinfo.Role;
import com.maidgroup.maidgroup.security.Password;
import com.maidgroup.maidgroup.service.UserService;
import com.maidgroup.maidgroup.service.exceptions.*;
import com.maidgroup.maidgroup.util.dto.LoginCreds;
import com.maidgroup.maidgroup.util.dto.Requests.UserRequest;
import com.maidgroup.maidgroup.util.dto.Responses.UserResponse;
import com.maidgroup.maidgroup.util.tokens.JWTUtility;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;


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
        user.setRole(Role.USER);

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
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);

        return userResponse;
    }

    @ExceptionHandler({InvalidCredentialsException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody String exceptionInvalidUserInput(InvalidCredentialsException e){
        return e.getMessage();
    }

    @ExceptionHandler({UsernameAlreadyExists.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody String exceptionUsernameExists(UsernameAlreadyExists e) { return e.getMessage(); }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody UserRequest logoutRequest, HttpServletRequest request, HttpServletResponse response){
        String jwt = logoutRequest.getJwt();
        if(jwt == null) {
            if (jwt == null) {
                jwt = extractJwt(request);
            }
        }
        userService.logout(jwt);
        Cookie cookie = new Cookie("jwt", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private String extractJwt(HttpServletRequest request){
        if(request.getCookies() != null){
            for (Cookie cookie : request.getCookies()) {
                if(cookie.getName().equals("jwt")){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId, @RequestBody UserRequest userRequest){
        // Check if the user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = new User();
        user.setUserId(userId);
        user.setUsername(userRequest.getUsername());
        user.setPassword(new Password(userRequest.getPassword()));
        user.setRawPassword(userRequest.getPassword());
        user.setConfirmPassword(new Password(userRequest.getConfirmPassword()));
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setGender(userRequest.getGender());
        user.setDateOfBirth(userRequest.getDateOfBirth());
        // Set the role if it is provided in the request body
        if (userRequest.getRole() != null) {
            user.setRole(userRequest.getRole());
        }

        User updatedUser = userService.updateUser(user);
        UserResponse userResponse = new UserResponse(updatedUser);

        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @GetMapping("/getAllUsers")
    public @ResponseBody List<UserResponse> getAllUsers(Principal principal){
        User authUser = userRepository.findByUsername(principal.getName());
        List<UserResponse> allUsers = userService.getAllUsers(authUser);
        return allUsers;
    }

    @GetMapping("/{username}")
    public UserResponse getByUsername(@PathVariable("username") String username, Principal principal){
        User authUser = userRepository.findByUsername(principal.getName());
        User user = userService.getByUsername(username, authUser);
        UserResponse userResponse = new UserResponse(user);
        return userResponse;
    }

    @DeleteMapping("/{userId}")
    public String delete(@PathVariable("userId") Long userId, Principal principal) {
        User authUser = userRepository.findByUsername(principal.getName());
        userService.delete(userId, authUser);
        return "Your account has been deleted";
    }

    @ExceptionHandler({UserNotFoundException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public @ResponseBody String handleUserNotFoundException(UserNotFoundException e) {
        return e.getMessage();
    }

    @ExceptionHandler({UnauthorizedException.class})
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public @ResponseBody String handleUnauthorizedException(UnauthorizedException e) {
        return e.getMessage();
    }

    @ExceptionHandler({InvalidCredentialsException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody String handleInvalidCredentialsException(InvalidCredentialsException e) {
        return e.getMessage();
    }
    @ExceptionHandler({InvalidDateException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody String handleInvalidDateException(InvalidDateException e) {
        return e.getMessage();
    }


}

