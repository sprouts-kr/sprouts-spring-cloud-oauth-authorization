package kr.sprouts.framework.service.oauth.authorization.applications.authorize.dto.proxy;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class AuthorizationProxy {
    private String header;
    private String value;
}
