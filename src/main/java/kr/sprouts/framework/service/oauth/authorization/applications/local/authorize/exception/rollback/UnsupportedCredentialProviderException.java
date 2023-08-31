package kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.exception.rollback;

import kr.sprouts.framework.autoconfigure.web.response.components.base.BaseRollbackException;
import org.springframework.http.HttpStatus;

public class UnsupportedCredentialProviderException extends BaseRollbackException {
    public UnsupportedCredentialProviderException() {
        super("unsupported_credential_provider", "Unsupported credential provider.", HttpStatus.BAD_REQUEST);
    }
}
