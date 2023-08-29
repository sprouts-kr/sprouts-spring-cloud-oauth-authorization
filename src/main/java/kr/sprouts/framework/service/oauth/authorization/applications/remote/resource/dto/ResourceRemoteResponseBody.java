package kr.sprouts.framework.service.oauth.authorization.applications.remote.resource.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
public class ResourceRemoteResponseBody<T> {
    @Getter
    private Boolean succeeded = Boolean.FALSE;
    private T content;

    public Optional<T> getContent() {
        return Optional.ofNullable(content);
    }
}
