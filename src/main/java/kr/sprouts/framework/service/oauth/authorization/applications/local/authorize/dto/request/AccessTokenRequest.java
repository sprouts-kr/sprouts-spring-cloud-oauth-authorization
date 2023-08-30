package kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.dto.request;

import lombok.Data;

@Data
public class AccessTokenRequest {
    private String email;
    private String password;
}
