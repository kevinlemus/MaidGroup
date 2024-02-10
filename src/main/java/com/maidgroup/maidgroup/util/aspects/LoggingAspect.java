package com.maidgroup.maidgroup.util.aspects;



import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Log4j2
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("within(com.maidgroup.maidgroup.*)")
    public void pcLogAll(){}

    @Before("pcLogAll()")
    public void logMethodStart(JoinPoint joinPoint){
        String methodArguments = Arrays.toString(joinPoint.getArgs());

        log.info("{} was successfully invoked at {} with the provided arguments: {}", extractClassMethodSignature(joinPoint), LocalDateTime.now(), methodArguments);
    }

    @AfterReturning(pointcut = "pcLogAll()", returning = "returnedObject")
    public void logMethodReturn(JoinPoint joinPoint, Object returnedObject){
        log.info("{} successfully returned at {} with value: {}", extractClassMethodSignature(joinPoint), LocalDateTime.now(), returnedObject);

    }

    @AfterThrowing(pointcut = "pcLogAll()", throwing = "t")
    public void logMethodThrowable(JoinPoint joinPoint, Throwable t){
        String throwableName = t.getClass().getName();
        log.warn("{} was thrown in {} at {} with a message: {}", throwableName, extractClassMethodSignature(joinPoint), LocalDateTime.now(), t.getMessage());
    }

    private String extractClassMethodSignature(JoinPoint joinPoint){
        return joinPoint.getTarget().getClass().toString() + "#" + joinPoint.getSignature().getName();
    }
}
