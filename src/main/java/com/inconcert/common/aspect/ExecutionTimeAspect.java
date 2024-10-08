package com.inconcert.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionTimeAspect {

    @Around("@annotation(com.inconcert.common.annotation.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();                    // 시작 시간 기록
        Object result = joinPoint.proceed();                            // 메서드 실행
        long endTime = System.currentTimeMillis();                      // 종료 시간 기록
        double executionTimeInSeconds = (endTime - startTime) / 1000.0; // ms를 초 단위로 변환

        System.out.println("스크래핑 실행 시간: " + executionTimeInSeconds + "초");
        return result;
    }
}