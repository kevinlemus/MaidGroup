package com.maidgroup.maidgroup.controller;

import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.service.UserService;
import com.maidgroup.maidgroup.service.exceptions.UnauthorizedException;
import com.maidgroup.maidgroup.service.exceptions.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User loginRequest, HttpServletRequest request){
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword().toString();
        boolean loggedIn = userService.login(username, password, request);

        if(loggedIn){
            User user = userRepository.findById(username).get();
            String message = "You have successfully logged in";
            return ResponseEntity.ok()
                    .header("loggedIn", "Success")
                    .header("message", message)
                    .body(user);

        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<User> logout(@RequestBody User logoutRequest, HttpServletRequest request){
        userService.logout(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/registerUser")
    public ResponseEntity<User> register(@RequestBody User user){
        return new ResponseEntity<User>(userService.register(user), HttpStatus.CREATED);
    }

    @PostMapping("{username}")
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
}
