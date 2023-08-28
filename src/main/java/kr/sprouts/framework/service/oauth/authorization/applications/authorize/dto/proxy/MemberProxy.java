package kr.sprouts.framework.service.oauth.authorization.applications.authorize.dto.proxy;

import kr.sprouts.framework.service.oauth.authorization.applications.remote.resource.dto.response.MemberRemoteResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class MemberProxy {
    private UUID id;
    private String email;
    private String name;
    private Boolean passwordExpired;
    private String passwordExpireDate;
    private String description;

    public static MemberProxy fromRemoteResponse(MemberRemoteResponse response) {
        return new MemberProxy(response.getId(),
                response.getEmail(),
                response.getName(),
                response.getPasswordExpired(),
                response.getPasswordExpireDate(),
                response.getDescription());
    }
}
