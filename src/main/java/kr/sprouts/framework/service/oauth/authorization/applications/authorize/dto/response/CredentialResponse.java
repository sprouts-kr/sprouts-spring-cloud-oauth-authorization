package kr.sprouts.framework.service.oauth.authorization.applications.authorize.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.sprouts.framework.autoconfigure.web.response.components.base.BaseResponse;
import kr.sprouts.framework.service.oauth.authorization.applications.authorize.dto.proxy.CredentialProxy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CredentialResponse extends BaseResponse {
    private AuthorizationResponse authorization;
    private MemberResponse member;

    public static CredentialResponse fromProxy(CredentialProxy proxy) {
        return new CredentialResponse(
                AuthorizationResponse.fromProxy(proxy.getAuthorization()),
                MemberResponse.fromProxy(proxy.getMember())
        );
    }
}
