package kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.dto.proxy;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PUBLIC, staticName = "of")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class BearerTokenProxy {
    private String header;
    private String value;
    private Long validityInMinutes;
}
