package com.attus.financial.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class AuditAspect {

    @Around("execution(* com.attus.financial.service.TransactionService.execute(..))")
    public Object logTransaction(ProceedingJoinPoint jp) throws Throwable {
        long start = System.currentTimeMillis();
        log.info("[AOP] Iniciando transação financeira");
        try {
            Object result = jp.proceed();
            log.info("[AOP] Transação concluída em {}ms", System.currentTimeMillis() - start);
            return result;
        } catch (Exception e) {
            log.error("[AOP] Falha na transação: {} em {}ms", e.getMessage(), System.currentTimeMillis() - start);
            throw e;
        }
    }
}
