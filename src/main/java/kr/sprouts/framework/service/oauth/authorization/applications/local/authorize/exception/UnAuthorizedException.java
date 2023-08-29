package kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.exception;

import kr.sprouts.framework.autoconfigure.web.response.components.base.BaseCommitException;
import org.springframework.http.HttpStatus;

public class UnAuthorizedException extends BaseCommitException {
    public UnAuthorizedException() {
        super("unauthorized", "Unauthorized", HttpStatus.UNAUTHORIZED);
    }
}
