package com.maidgroup.maidgroup.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maidgroup.maidgroup.service.CustomUserDetailsService;
import com.maidgroup.maidgroup.service.FacebookOAuth2UserService;
import com.maidgroup.maidgroup.service.GoogleOAuth2UserService;
import com.maidgroup.maidgroup.util.square.mock.SquareClientWrapper;
import com.maidgroup.maidgroup.util.square.mock.SquareClientWrapperImpl;
import com.maidgroup.maidgroup.util.tokens.JWTAuthenticationFilter;
import com.maidgroup.maidgroup.util.tokens.JWTUtility;
import com.squareup.square.Environment;
import com.squareup.square.SquareClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    CustomUserDetailsService customUserDetailsService;
    JWTUtility jwtUtility;
    JWTAuthenticationFilter jwtAuthenticationFilter;
    GoogleOAuth2UserService googleOAuth2UserService;
    FacebookOAuth2UserService facebookOAuth2UserService;
    @Value("${square.access-token}")
    private String squareAccessToken;

    @Autowired
    public WebSecurityConfig(CustomUserDetailsService customUserDetailsService, JWTUtility jwtUtility, JWTAuthenticationFilter jwtAuthenticationFilter, GoogleOAuth2UserService googleOAuth2UserService, FacebookOAuth2UserService facebookOAuth2UserService) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtility = jwtUtility;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.googleOAuth2UserService = googleOAuth2UserService;
        this.facebookOAuth2UserService = facebookOAuth2UserService;
    }

    @Bean
    public JWTAuthenticationFilter jwtAuthenticationFilter() {
        return new JWTAuthenticationFilter(jwtUtility, customUserDetailsService);
    }

    @Bean
    public PasswordConverter passwordConverter() {
        return new PasswordConverter();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SquareClientWrapper squareClientWrapper() {
        // Create a new instance of SquareClient
        SquareClient squareClient = new SquareClient.Builder()
                .environment(Environment.SANDBOX) // or Environment.PRODUCTION
                .accessToken(squareAccessToken)
                .build();

        // Return a new instance of your SquareClientWrapper implementation
        return new SquareClientWrapperImpl(squareClient);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .requestMatchers(HttpMethod.POST, "/user/login", "/user/registerUser", "/user/logout", "/user/forgotPassword", "/user/resetPassword", "/consultation/create").permitAll()
                .requestMatchers(HttpMethod.GET, "/consultation/{id}", "/invoice/{id}").access("hasAuthority('ADMIN') or (isAuthenticated() and #id.toString().equals(authentication.principal.userId.toString()))")
                .requestMatchers(HttpMethod.GET, "/consultation/getConsultations").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/consultation/deleteConsultations").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/consultation/cancel/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/consultation/link/{uniqueLink}").permitAll()
                .requestMatchers(HttpMethod.POST, "/user/{userId}/deactivate").access("hasAuthority('ADMIN') or (isAuthenticated() and #userId.toString().equals(authentication.principal.userId.toString()))")
                .requestMatchers(HttpMethod.PUT, "/user/{userId}").access("hasAuthority('ADMIN') or (isAuthenticated() and #userId.toString().equals(authentication.principal.userId.toString()))")
                .requestMatchers(HttpMethod.GET, "/user/getAllUsers").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/user/{username}").access("hasAuthority('ADMIN') or (isAuthenticated() and #username.equals(authentication.principal.username))")
                .requestMatchers(HttpMethod.DELETE, "/user/{userId}").access("hasAuthority('ADMIN') or (isAuthenticated() and #userId.toString().equals(authentication.principal.userId.toString()))")
                .requestMatchers(HttpMethod.POST, "/consultation/create", "/user/create", "/invoice/create").permitAll()
                .requestMatchers(HttpMethod.POST, "/invoice/create").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.POST, "/invoices/webhook").permitAll()
                .requestMatchers(HttpMethod.GET, "/invoices/getInvoices").access("hasAuthority('ADMIN') or isAuthenticated()")
                .requestMatchers(HttpMethod.GET, "/invoice/orderId/{orderId}").access("hasAuthority('ADMIN') or (isAuthenticated() and #orderId == authentication.principal.orderId)")
                .requestMatchers(HttpMethod.PUT, "/invoice/{invoiceId}").access("hasAuthority('ADMIN') and @securityService.isNotPaid(#invoiceId)")
                .requestMatchers(HttpMethod.POST, "/invoice/sendLink/{orderId}").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.POST, "/invoice/sendInvoice/{orderId}").access("hasAuthority('ADMIN') or (isAuthenticated() and #orderId == authentication.principal.orderId)")
                .requestMatchers(new AntPathRequestMatcher("/maidgroup/v3/**"), new AntPathRequestMatcher("/maidgroup/swagger-ui/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/swagger-config")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/controller-api")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/error")).permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    protected void configure(HttpSecurity http) throws Exception {
        http
                .oauth2Login()
                .userInfoEndpoint()
                .userService(googleOAuth2UserService)  // For Google
                .userService(facebookOAuth2UserService)  // For Facebook
                //.userService(appleOAuth2UserService)  // For Apple
                .and()
                .defaultSuccessUrl("/home", true)
                .failureHandler(new SimpleUrlAuthenticationFailureHandler() {
                    @Override
                    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                        super.onAuthenticationFailure(request, response, exception);
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        Map<String, Object> data = new HashMap<>();
                        data.put("timestamp", Calendar.getInstance().getTime());
                        data.put("exception", exception.getMessage());
                        response.getOutputStream().println(new ObjectMapper().writeValueAsString(data));
                    }
                });
    }

}
