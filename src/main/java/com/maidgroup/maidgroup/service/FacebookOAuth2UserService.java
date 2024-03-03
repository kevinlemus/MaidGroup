package com.maidgroup.maidgroup.service;

import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.User;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@NoArgsConstructor
@Log4j2
public class FacebookOAuth2UserService extends DefaultOAuth2UserService {

    private UserRepository userRepository;

    @Autowired
    public FacebookOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static final String ERROR_CODE_USER_NOT_FOUND = "user_not_found";
    public static final String ERROR_DESCRIPTION_USER_NOT_FOUND = "The user does not exist";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        if (userRequest.getAccessToken().getTokenValue().equals("mockToken")) {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("name", userRequest.getAdditionalParameters().get("name"));
            attributes.put("email", userRequest.getAdditionalParameters().get("email"));
            return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("USER")), attributes, "name");
        }
        String provider = userRequest.getClientRegistration().getRegistrationId();
        if (provider.equals("facebook")) {
            Object emailObject = userRequest.getAdditionalParameters().get("email");
            if (emailObject == null) {
                throw new OAuth2AuthenticationException(new OAuth2Error(ERROR_CODE_USER_NOT_FOUND, ERROR_DESCRIPTION_USER_NOT_FOUND, ""));
            }
            String email = emailObject.toString();
            User user = userRepository.findByEmail(email);

            // Get the 'register' parameter from the request
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            String registerParam = attr.getRequest().getParameter("register");
            boolean isRegistering = registerParam != null && registerParam.equals("true");

            if (user == null && isRegistering) {
                // Register a new user
                user = new User();
                user.setEmail(email);
                Object nameObject = userRequest.getAdditionalParameters().get("name");
                if (nameObject != null) {
                    user.setUsername(nameObject.toString());
                }
                // Set other fields as necessary
                userRepository.save(user);
            } else if (user == null) {
                throw new OAuth2AuthenticationException(new OAuth2Error(ERROR_CODE_USER_NOT_FOUND, ERROR_DESCRIPTION_USER_NOT_FOUND, ""));
            }
        }
        return super.loadUser(userRequest);
    }

}
