package kr.sprouts.framework.oauth.authorization.components.aop;

import kr.sprouts.framework.autoconfigure.web.response.components.base.BaseException;
import kr.sprouts.framework.autoconfigure.web.response.components.entity.StructuredResponse;
import kr.sprouts.framework.autoconfigure.web.response.components.entity.StructuredResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestControllerAdvisor {
    @ExceptionHandler(BaseException.class)
    public StructuredResponseEntity exception(BaseException baseException) {
        return StructuredResponse.error(baseException);
    }
}
