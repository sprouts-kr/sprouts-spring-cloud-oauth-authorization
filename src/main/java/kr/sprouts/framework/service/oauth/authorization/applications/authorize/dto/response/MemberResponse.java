package kr.sprouts.framework.service.oauth.authorization.applications.authorize.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.sprouts.framework.autoconfigure.web.response.components.base.BaseResponse;
import kr.sprouts.framework.service.oauth.authorization.applications.authorize.dto.proxy.MemberProxy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class MemberResponse extends BaseResponse {
    private UUID id;
    private String email;
    private String name;
    private Boolean passwordExpired;
    private String passwordExpireDate;
    private String description;

    public static MemberResponse fromProxy(MemberProxy proxy) {
        return new MemberResponse(
                proxy.getId(),
                proxy.getEmail(),
                proxy.getName(),
                proxy.getPasswordExpired(),
                proxy.getPasswordExpireDate(),
                proxy.getDescription()
        );
    }
}
