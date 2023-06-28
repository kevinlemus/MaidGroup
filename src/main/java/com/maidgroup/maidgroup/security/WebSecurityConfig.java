package com.maidgroup.maidgroup.security;

import com.maidgroup.maidgroup.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordConverter passwordConverter() {
        return new PasswordConverter();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

   /* @Bean
    @Primary
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .authorizeRequests(auth -> auth
                        .requestMatchers("/getAllUsers").hasRole("Admin")
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/registerUser").permitAll()
                        .anyRequest().authenticated()
                )
                //.and()
                //.formLogin()
                //.loginPage("/login")
                //.and()
                .logout()
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)//any resources/data associated with the session cookie will be lost/released
                .deleteCookies("JSESSIONID")//deleting session cookie, JSESSIONID being the default session cookie name
                .and()
                .csrf() // enable csrf protection
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()); // use cookie as csrf token repository
        return http.build();
    }*/

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.csrf().disable();
        http.userDetailsService(customUserDetailsService);
        return http.build();
    }



}
