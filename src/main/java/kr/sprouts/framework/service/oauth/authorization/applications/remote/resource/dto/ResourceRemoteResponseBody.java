package kr.sprouts.framework.service.oauth.authorization.applications.remote.resource.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class ResourceRemoteResponseBody<T> {
    private Boolean succeeded;
    private T content;
}
