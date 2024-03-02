package com.maidgroup.maidgroup.controller;

import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/authSuccess")
    public String handleAuthentication(Model model, OAuth2AuthenticationToken authentication) {
        OAuth2User oAuth2User = authentication.getPrincipal();
        String username = oAuth2User.getAttribute("name");
        User user = userRepository.findByUsername(username);
        if (user != null) {
            model.addAttribute("user", user);
        } else {
            // Handle the case where the user is not found in the database
            model.addAttribute("error", "User not found");
        }
        return "authSuccess";
    }
}
