package com.maidgroup.maidgroup.util.aspects;

import com.maidgroup.maidgroup.dao.Secured;
import com.maidgroup.maidgroup.service.exceptions.UnauthorizedException;
import com.maidgroup.maidgroup.util.tokens.JWTUtility;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class AuthAspect {
    private final JWTUtility jwtUtility;

    @Autowired
    public AuthAspect(JWTUtility jwtUtility){
        this.jwtUtility = jwtUtility;
    }

    @Around("@annotation(com.maidgroup.maidgroup.dao.Secured)")
    public Object securedEndpoint(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Secured annotation = method.getAnnotation(Secured.class);

        String token = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getHeader("Authorization");
        if(!jwtUtility.isTokenValid(token)) throw new UnauthorizedException("No token found");

        if(!annotation.isAdmin() && !jwtUtility.extractTokenDetails(token).getRole().equals("ADMIN")){
            throw new UnauthorizedException("User does not have administrative permission to access this endpoint.");
        }

        return pjp.proceed();
    }

}
