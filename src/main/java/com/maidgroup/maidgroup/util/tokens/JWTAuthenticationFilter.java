package com.maidgroup.maidgroup.util.tokens;

import com.maidgroup.maidgroup.service.CustomUserDetailsService;
import com.maidgroup.maidgroup.util.dto.PrincipalUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTAuthenticationFilter extends OncePerRequestFilter {
//OncePerRequestFilter is responsible for validating the JWT token and authenticating the user.
    private final JWTUtility jwtUtility;
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    public JWTAuthenticationFilter(JWTUtility jwtUtility, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtility = jwtUtility;
        this.customUserDetailsService = customUserDetailsService;
    }
    public JWTAuthenticationFilter(JWTUtility jwtUtility) {
        this.jwtUtility = jwtUtility;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");//extracting the Authorization header from the request.

        if (header == null || !header.startsWith("Bearer ")) {//if the header doesn't start with "Bearer" or is null, we're skipping and calling filterChain.doFilter() method.
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.replace("Bearer ", "");//if the header does start with "Bearer" we're extracting the JWT token from the header by removing the "Bearer" prefix.

        try {
            PrincipalUser principalUser = jwtUtility.extractTokenDetails(token);//we're then using JWTUtility class to validate the token and extract the principal user details.

            if (principalUser != null && SecurityContextHolder.getContext().getAuthentication() == null) {//if the token is valid and there is no existing authentication object in the security context
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(principalUser.getUsername());//we're loading the user details from the database using the CustomUserDetailsService class

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());//we're then creating a new UsernamePasswordAuthenticationToken object
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));//we set those details in the security context
                SecurityContextHolder.getContext().setAuthentication(authentication);//in order to authenticate the user
            }
        } catch (Exception e) {
            // handle exception
        }

        filterChain.doFilter(request, response);
    }
}
