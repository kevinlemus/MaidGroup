package com.maidgroup.maidgroup.security;

import com.maidgroup.maidgroup.service.CustomUserDetailsService;
import com.maidgroup.maidgroup.util.square.mock.SquareClientWrapper;
import com.maidgroup.maidgroup.util.square.mock.SquareClientWrapperImpl;
import com.maidgroup.maidgroup.util.tokens.JWTAuthenticationFilter;
import com.maidgroup.maidgroup.util.tokens.JWTUtility;
import com.squareup.square.Environment;
import com.squareup.square.SquareClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    String USER_PRINCIPAL_SERVICE = "(com.maidgroup.maidgroup.security.CustomUserPrincipal)";
    CustomUserDetailsService customUserDetailsService;
    JWTUtility jwtUtility;
    JWTAuthenticationFilter jwtAuthenticationFilter;
    @Value("${square.access-token}")
    private String squareAccessToken;

    @Autowired
    public WebSecurityConfig(CustomUserDetailsService customUserDetailsService, JWTUtility jwtUtility, JWTAuthenticationFilter jwtAuthenticationFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtility = jwtUtility;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
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
                .requestMatchers(HttpMethod.GET, "/{id}").access("hasAuthority('ADMIN') or (isAuthenticated() and #id == " + USER_PRINCIPAL_SERVICE + ".fromAuthentication(authentication).getUserId())")
                .requestMatchers(HttpMethod.GET, "/getConsultations").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/deleteConsultations", "/{id}").access("hasAuthority('ADMIN') or (isAuthenticated() and #id == " + USER_PRINCIPAL_SERVICE + ".fromAuthentication(authentication).getUserId())")
                .requestMatchers(HttpMethod.PUT, "/consultation/cancel/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/consultation/link/{uniqueLink}").permitAll()
                .requestMatchers(HttpMethod.POST, "/user/{id}/deactivate").access("hasAuthority('ADMIN') or (isAuthenticated() and #id == " + USER_PRINCIPAL_SERVICE + ".fromAuthentication(authentication).getUserId())")
                .requestMatchers(HttpMethod.PUT, "/user/{userId}").access("hasAuthority('ADMIN') or (isAuthenticated() and #userId == " + USER_PRINCIPAL_SERVICE + ".fromAuthentication(authentication).getUserId())")
                .requestMatchers(HttpMethod.GET, "/user/getAllUsers").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/user/{username}").access("hasAuthority('ADMIN') or (isAuthenticated() and #username == " + USER_PRINCIPAL_SERVICE + ".fromAuthentication(authentication).getUsername())")
                .requestMatchers(HttpMethod.DELETE, "/user/{userId}").access("hasAuthority('ADMIN') or (isAuthenticated() and #userId == " + USER_PRINCIPAL_SERVICE + ".fromAuthentication(authentication).getUserId())")
                .requestMatchers(HttpMethod.POST, "/create").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.POST, "/webhook").permitAll()
                .requestMatchers(HttpMethod.GET, "/getInvoices").access("hasAuthority('ADMIN') or isAuthenticated()")
                .requestMatchers(HttpMethod.GET, "/{id}").access("hasAuthority('ADMIN') or (isAuthenticated() and #id == " + USER_PRINCIPAL_SERVICE + ".fromAuthentication(authentication).getUserId())")
                .requestMatchers(HttpMethod.GET, "/orderId/{orderId}").access("hasAuthority('ADMIN') or (isAuthenticated() and #orderId == " + USER_PRINCIPAL_SERVICE + ".fromAuthentication(authentication).getOrderId())")
                .requestMatchers(HttpMethod.PUT, "/{invoiceId}").access("hasAuthority('ADMIN') and @securityService.isNotPaid(#invoiceId)")
                .requestMatchers(HttpMethod.POST, "/sendLink/{orderId}").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.POST, "/sendInvoice/{orderId}").access("hasAuthority('ADMIN') or (isAuthenticated() and #orderId == " + USER_PRINCIPAL_SERVICE + ".fromAuthentication(authentication).getOrderId())")
                .requestMatchers(new AntPathRequestMatcher("/invoices/webhook")).permitAll()
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










}
