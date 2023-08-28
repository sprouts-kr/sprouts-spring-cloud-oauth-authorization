package kr.sprouts.framework.service.oauth.authorization.applications.authorize.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.sprouts.framework.service.oauth.authorization.applications.authorize.dto.proxy.AuthorizationProxy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class AuthorizationResponse {
    private String header;
    private String value;

    public static AuthorizationResponse fromProxy(AuthorizationProxy proxy) {
        return new AuthorizationResponse(proxy.getHeader(), proxy.getValue());
    }
}
