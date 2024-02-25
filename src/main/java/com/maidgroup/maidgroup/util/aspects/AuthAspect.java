package com.maidgroup.maidgroup.util.aspects;

import com.maidgroup.maidgroup.dao.Secured;
import com.maidgroup.maidgroup.model.userinfo.Role;
import com.maidgroup.maidgroup.service.exceptions.UnauthorizedException;
import com.maidgroup.maidgroup.util.tokens.JWTUtility;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Log4j2
@Aspect
@Component
public class AuthAspect {
    private final JWTUtility jwtUtility;

    @Autowired
    public AuthAspect(JWTUtility jwtUtility) {
        this.jwtUtility = jwtUtility;
    }

    @Around("@annotation(com.maidgroup.maidgroup.dao.Secured)")
    public Object securedEndpoint(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Secured annotation = method.getAnnotation(Secured.class);

        String authHeader = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getHeader("Authorization");
        String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;

        if (token == null || !jwtUtility.isTokenValid(token)) throw new UnauthorizedException("No token found");

        // Extract role from token
        String roleFromToken = jwtUtility.extractTokenDetails(token).getRole();
        log.info("Role from token: {}", roleFromToken);

        // Check if user is admin
        boolean isAdmin = annotation.isAdmin();
        log.info("isAdmin: {}", isAdmin);

        if(annotation.isAdmin() && !Role.valueOf(jwtUtility.extractTokenDetails(token).getRole()).equals(Role.ADMIN)){
            throw new UnauthorizedException("User does not have administrative permission to access this endpoint.");
        }


        return pjp.proceed();
    }
}
