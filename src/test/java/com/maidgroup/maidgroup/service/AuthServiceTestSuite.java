package com.maidgroup.maidgroup.service;

import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthServiceTestSuite {

    @InjectMocks
    private GoogleOAuth2UserService sut;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DefaultOAuth2UserService defaultOAuth2UserService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Before
    public void testPrep() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_loadUser_returnsOAuth2User_whenUserExists() {
        // Arrange
        String provider = "google";
        String username = "testuser";
        String email = "testuser@example.com";
        User expectedUser = new User();
        expectedUser.setUsername(username);
        expectedUser.setEmail(email);

        OAuth2User oAuth2User = createOAuth2User(username, email);
        OAuth2UserRequest userRequest = createOAuth2UserRequest(oAuth2User, provider);

        when(userRepository.findByUsername(any(String.class))).thenReturn(expectedUser);
        when(defaultOAuth2UserService.loadUser(any(OAuth2UserRequest.class))).thenReturn(oAuth2User);

        // Act
        OAuth2User actualOAuth2User = sut.loadUser(userRequest);

        // Assert
        assertEquals(oAuth2User.getAttributes(), actualOAuth2User.getAttributes());
    }

    @Test
    public void test_loadUser_throwsException_whenUserDoesNotExist() {
        // Arrange
        String provider = "google";
        String username = "testuser";
        String email = "testuser@example.com";

        OAuth2User oAuth2User = createOAuth2User(username, email);
        OAuth2UserRequest userRequest = createOAuth2UserRequest(oAuth2User, provider);

        when(userRepository.findByUsername(any(String.class))).thenReturn(null);

        // Act
        OAuth2User actualOAuth2User = null;
        try {
            actualOAuth2User = sut.loadUser(userRequest);
        } catch (OAuth2AuthenticationException e) {
            // Assert
            assertEquals("user_not_found", e.getError().getErrorCode());
        }
        assertNull(actualOAuth2User);
    }

    @Test
    public void test_loadUser_createsNewUser_whenUserIsRegistering() {
        // Arrange
        String provider = "google";
        String username = "testuser";
        String email = "testuser@example.com";

        OAuth2User oAuth2User = createOAuth2User(username, email);
        OAuth2UserRequest userRequest = createOAuth2UserRequest(oAuth2User, provider);

        when(userRepository.findByUsername(any(String.class))).thenReturn(null);

        // Mock RequestContextHolder
        ServletRequestAttributes attr = mock(ServletRequestAttributes.class);
        when(attr.getRequest().getParameter("register")).thenReturn("true");
        RequestContextHolder.setRequestAttributes(attr);

        // Act
        OAuth2User actualOAuth2User = sut.loadUser(userRequest);

        // Assert
        assertEquals(oAuth2User.getAttributes(), actualOAuth2User.getAttributes());
    }


    private OAuth2User createOAuth2User(String username, String email) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", username);
        attributes.put("email", email);
        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("USER")), attributes, "name");
    }

    private OAuth2UserRequest createOAuth2UserRequest(OAuth2User oAuth2User, String provider) {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(provider)
                .clientId(clientId)  // Replace with your actual client ID
                .clientSecret(clientSecret)  // Replace with your actual client secret
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .scope("openid", "profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
                .userNameAttributeName("sub")
                .clientName("Google")
                .build();
        return new OAuth2UserRequest(clientRegistration, new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "access-token", null, null));
    }

}


