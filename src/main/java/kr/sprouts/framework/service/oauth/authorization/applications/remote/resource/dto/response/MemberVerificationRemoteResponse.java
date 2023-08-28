package kr.sprouts.framework.service.oauth.authorization.applications.remote.resource.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class MemberVerificationRemoteResponse {
    private Boolean isVerification;
    @Getter
    private MemberRemoteResponse member;

    public Boolean isVerification() {
        return isVerification;
    }
}
