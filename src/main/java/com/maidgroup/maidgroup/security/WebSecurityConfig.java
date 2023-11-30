package com.maidgroup.maidgroup.security;

import com.maidgroup.maidgroup.service.CustomUserDetailsService;
import com.maidgroup.maidgroup.util.tokens.JWTAuthenticationFilter;
import com.maidgroup.maidgroup.util.tokens.JWTUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .requestMatchers(new AntPathRequestMatcher("/user/login"), new AntPathRequestMatcher("/user/registerUser")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/invoices/webhook")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/delete")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/getAllUsers")).hasAuthority("ADMIN")
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
