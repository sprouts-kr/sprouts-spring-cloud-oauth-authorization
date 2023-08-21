package kr.sprouts.framework.oauth.authorization.components.aop;

import kr.sprouts.autoconfigure.response.base.BaseException;
import kr.sprouts.autoconfigure.response.exception.UnhandledException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class RestControllerAspect {
    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping)"
            + " || @annotation(org.springframework.web.bind.annotation.PostMapping)"
            + " || @annotation(org.springframework.web.bind.annotation.PutMapping)"
            + " || @annotation(org.springframework.web.bind.annotation.PatchMapping)"
            + " || @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    private void onRequest() { }

    @Around("onRequest()")
    private Object doRequest(ProceedingJoinPoint proceedingJoinPoint) {
        try {
            return proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
        } catch (BaseException baseException) {
            throw baseException;
        } catch (Throwable throwable) {
            throw UnhandledException.of(throwable);
        }
    }
}
