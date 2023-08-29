package kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class CredentialRequest {
    private String email;
    private String password;
}
