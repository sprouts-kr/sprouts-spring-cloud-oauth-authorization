package kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.dto.request;

import lombok.Data;

@Data
public class AuthorizeRequest {
    private String email;
    private String password;
}
