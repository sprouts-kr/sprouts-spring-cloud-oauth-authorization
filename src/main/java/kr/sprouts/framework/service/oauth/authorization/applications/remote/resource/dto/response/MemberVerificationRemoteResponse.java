package kr.sprouts.framework.service.oauth.authorization.applications.remote.resource.dto.response;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
public class MemberVerificationRemoteResponse {
    private Boolean isVerification;
    private MemberRemoteResponse member;

    public Boolean isVerification() {
        return isVerification;
    }

    public Optional<MemberRemoteResponse> getMember() {
        return Optional.ofNullable(member);
    }
}
