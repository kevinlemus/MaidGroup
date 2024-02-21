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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    CustomUserDetailsService customUserDetailsService;
    JWTUtility jwtUtility;
    @Value("${square.access-token}")
    private String squareAccessToken;

    @Autowired
    public WebSecurityConfig(CustomUserDetailsService customUserDetailsService, JWTUtility jwtUtility) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtility = jwtUtility;
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
                .requestMatchers(new AntPathRequestMatcher("/user/login"), new AntPathRequestMatcher("/user/registerUser")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/invoices/webhook")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/delete")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/getAllUsers")).hasAuthority("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/maidgroup/v3/**"), new AntPathRequestMatcher("/maidgroup/swagger-ui/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/swagger-config")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/controller-api")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/error")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/maidgroup/consultation/cancel/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/maidgroup/consultation/link/**")).permitAll()
                //.anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }



}
