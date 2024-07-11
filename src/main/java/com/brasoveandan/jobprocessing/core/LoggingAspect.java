package com.brasoveandan.jobprocessing.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Log4j2
@Component
@RequiredArgsConstructor
public class LoggingAspect {
    private final ObjectMapper objectMapper;

    @Pointcut("within(com.brasoveandan.jobprocessing.controller.JobProcessingController)")
    public void restControllerMethods() {}

    @Around("restControllerMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String input = objectMapper.writeValueAsString(args);

        log.info("Entering method: {}.{}() with arguments = {}", className, methodName, input);

        Object result;
        try {
            result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - startTime;
            String output = objectMapper.writeValueAsString(result);

            log.info("Exiting method: {}.{}() with result = {} and execution time = {} ms", className, methodName, output, elapsedTime);
        } catch (Throwable throwable) {
            log.error("Exception in method: {}.{}() with arguments = {}", className, methodName, input, throwable);
            throw throwable;
        }

        return result;
    }
}