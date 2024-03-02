package com.maidgroup.maidgroup.service;

import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class GoogleOAuth2UserService extends DefaultOAuth2UserService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public GoogleOAuth2UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        if (provider.equals("google")) {
            String username = oAuth2User.getAttribute("name");
            String email = oAuth2User.getAttribute("email");
            User user = userRepository.findByUsername(username);

            // Get the 'register' parameter from the request
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            String registerParam = attr.getRequest().getParameter("register");
            boolean isRegistering = registerParam != null && registerParam.equals("true");

            if (user == null && isRegistering) {
                // Register a new user
                user = new User();
                user.setUsername(username);
                user.setEmail(email);
                user.setFirstName(oAuth2User.getAttribute("given_name"));
                user.setLastName(oAuth2User.getAttribute("family_name"));
                // Set other fields as necessary
                userRepository.save(user);
            } else if (user == null) {
                throw new OAuth2AuthenticationException(new OAuth2Error("user_not_found", "The user does not exist", ""));
            }
        }
        return oAuth2User;
    }


}
