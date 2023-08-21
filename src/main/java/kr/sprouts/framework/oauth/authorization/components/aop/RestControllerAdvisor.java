package kr.sprouts.framework.oauth.authorization.components.aop;

import kr.sprouts.autoconfigure.response.base.BaseException;
import kr.sprouts.autoconfigure.response.entity.StructuredResponse;
import kr.sprouts.autoconfigure.response.entity.StructuredResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestControllerAdvisor {
    @ExceptionHandler(BaseException.class)
    public StructuredResponseEntity exception(BaseException baseException) {
        return StructuredResponse.error(baseException);
    }
}
