package com.maidgroup.maidgroup.util.tokens;

import com.maidgroup.maidgroup.service.CustomUserDetailsService;
import com.maidgroup.maidgroup.util.dto.PrincipalUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Log4j2
@NoArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {
//OncePerRequestFilter is responsible for validating the JWT token and authenticating the user.
    private JWTUtility jwtUtility;
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    public JWTAuthenticationFilter(JWTUtility jwtUtility, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtility = jwtUtility;
        this.customUserDetailsService = customUserDetailsService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("Entering doFilterInternal method");

        String header = request.getHeader("Authorization");
        log.debug("Authorization header: {}", header);

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.replace("Bearer ", "");
        log.debug("Extracted token: {}", token);

        try {
            PrincipalUser principalUser = jwtUtility.extractTokenDetails(token);
            log.debug("Extracted principal user: {}", principalUser);

            if (principalUser != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(principalUser.getUsername());
                log.debug("Loaded user details: {}", userDetails);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("An error occurred while processing the JWT token", e);
        }

        filterChain.doFilter(request, response);
        log.debug("Exiting doFilterInternal method");

    }

}
