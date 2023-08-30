package kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.sprouts.framework.autoconfigure.web.response.components.base.BaseResponse;
import kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.dto.proxy.BearerTokenProxy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class BearerTokenResponse extends BaseResponse {
    private String header;
    private String value;
    private Long validityInMinutes;

    public static BearerTokenResponse fromProxy(BearerTokenProxy proxy) {
        return new BearerTokenResponse(proxy.getHeader(), proxy.getValue(), proxy.getValidityInMinutes());
    }
}
