package com.maidgroup.maidgroup.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    protected void configure(HttpSecurity http) throws Exception{
        http
                .authorizeRequests()
                .requestMatchers("/getAllUsers").hasRole("Admin")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
                .logout()
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)//any resources/data associated with the session cookie will be lost/released
                .deleteCookies("JSESSIONID");//deleting session cookie, JSESSIONID being the default session cookie name

    }
}
