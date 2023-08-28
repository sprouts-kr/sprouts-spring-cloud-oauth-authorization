package kr.sprouts.framework.service.oauth.authorization.applications.remote.resource.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class MemberVerificationRequest {
    private String email;
    private String password;

    public static MemberVerificationRequest of(String email, String password) {
        return new MemberVerificationRequest(email, password);
    }
}
