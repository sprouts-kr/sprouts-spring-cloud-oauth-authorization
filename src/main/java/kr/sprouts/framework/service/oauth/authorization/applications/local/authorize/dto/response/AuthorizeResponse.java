package kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.sprouts.framework.autoconfigure.web.response.components.base.BaseResponse;
import kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.dto.proxy.AuthorizeProxy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class AuthorizeResponse extends BaseResponse {
    private BearerTokenResponse accessToken;
    private BearerTokenResponse refreshToken;
    private MemberResponse member;

    public static AuthorizeResponse fromProxy(AuthorizeProxy proxy) {
        return new AuthorizeResponse(
                BearerTokenResponse.fromProxy(proxy.getAccessToken()),
                BearerTokenResponse.fromProxy(proxy.getRefreshToken()),
                MemberResponse.fromProxy(proxy.getMember())
        );
    }
}
