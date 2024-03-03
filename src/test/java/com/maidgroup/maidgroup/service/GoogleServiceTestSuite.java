package com.maidgroup.maidgroup.service;

import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoogleServiceTestSuite {

    @InjectMocks
    private GoogleOAuth2UserService sut;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DefaultOAuth2UserService defaultOAuth2UserService;

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

        when(userRepository.findByEmail(email)).thenReturn(expectedUser);
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

        // Create a new OAuth2UserRequest with the email set to null and the access token set to something other than "mockToken"
        Map<String, Object> parameters = new HashMap<>(userRequest.getAdditionalParameters());
        parameters.put("email", null);
        OAuth2UserRequest newUserRequest = new OAuth2UserRequest(userRequest.getClientRegistration(), new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "notMockToken", null, null), parameters);

        when(userRepository.findByUsername(any(String.class))).thenReturn(null);

        // Act and Assert
        OAuth2AuthenticationException exception = assertThrows(OAuth2AuthenticationException.class, () -> {
            sut.loadUser(newUserRequest);
        });

        String expectedErrorCode = "user_not_found";
        String actualErrorCode = exception.getError().getErrorCode();

        assertEquals(expectedErrorCode, actualErrorCode);
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

        // Mock HttpServletRequest
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("register")).thenReturn("true");

        // Create real ServletRequestAttributes with mock HttpServletRequest
        ServletRequestAttributes attr = new ServletRequestAttributes(request);
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
                .clientId("734260982519-dmps7kip5otvajts5v3jpsgkn4ontho7.apps.googleusercontent.com")  // Replace with your actual client ID
                .clientSecret("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")  // Replace with your actual client secret
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth") // Authorization URI
                .tokenUri("https://oauth2.googleapis.com/token") // Token URI
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo") // UserInfo URI
                .userNameAttributeName("sub") // Add this line
                .scope("openid", "profile", "email")
                .build();

        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put("email", oAuth2User.getAttributes().get("email"));
        additionalParameters.put("name", oAuth2User.getAttributes().get("name"));

        return new OAuth2UserRequest(clientRegistration, new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "mockToken", null, null), additionalParameters);    }

}



