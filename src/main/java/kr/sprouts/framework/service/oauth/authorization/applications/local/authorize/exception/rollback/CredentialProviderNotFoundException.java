package kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.exception.rollback;

import kr.sprouts.framework.autoconfigure.web.response.components.base.BaseRollbackException;
import org.springframework.http.HttpStatus;

public class CredentialProviderNotFoundException extends BaseRollbackException {
    public CredentialProviderNotFoundException() {
        super("credential_provider_not_found", "Credential provider not found.", HttpStatus.BAD_REQUEST);
    }
}
