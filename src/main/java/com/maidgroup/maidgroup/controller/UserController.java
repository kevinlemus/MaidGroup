package com.maidgroup.maidgroup.controller;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {
    UserService userService;
    UserRepository userRepository;
    JWTUtility jwtUtility;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository, JWTUtility jwtUtility){
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtUtility = jwtUtility;
    }

    @PostMapping("/login")
    public @ResponseBody User login(@RequestBody LoginCreds loginCreds, HttpServletResponse response){
        User authUser = userService.login(loginCreds.getUsername(), loginCreds.getPassword());
        String token = jwtUtility.createToken(authUser);
        response.setHeader("Authorization", token);
        return authUser;
    }

    @ExceptionHandler({InvalidCredentialsException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody String exceptionInvalidUserInput(InvalidCredentialsException ex){
        return ex.getMessage();
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
    public ResponseEntity<User> register(@RequestBody User user){
        return new ResponseEntity<User>(userService.register(user), HttpStatus.CREATED);
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

