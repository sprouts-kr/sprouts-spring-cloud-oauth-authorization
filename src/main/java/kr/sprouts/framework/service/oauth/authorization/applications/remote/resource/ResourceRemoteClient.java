package kr.sprouts.framework.service.oauth.authorization.applications.remote.resource;

import kr.sprouts.framework.service.oauth.authorization.applications.remote.resource.dto.ResourceRemoteResponseBody;
import kr.sprouts.framework.service.oauth.authorization.applications.remote.resource.dto.request.MemberVerificationRequest;
import kr.sprouts.framework.service.oauth.authorization.applications.remote.resource.dto.response.MemberVerificationRemoteResponse;
import kr.sprouts.framework.service.oauth.authorization.applications.remote.resource.exception.ResourceWebClientInitializeFailedException;
import kr.sprouts.framework.service.oauth.authorization.components.webflux.RemoteWebClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
@Slf4j
public class ResourceRemoteClient {
    private final org.springframework.web.reactive.function.client.WebClient webClient;

    public ResourceRemoteClient(@Value("${remote.resource.host}") String host,
                                @Value("${remote.resource.contextPath}") String contextPath,
                                @Value("${remote.resource.authorization.header}") String authorizationHeader,
                                @Value("${remote.resource.authorization.prefix}") String authorizationPrefix,
                                @Value("${remote.resource.authorization.value}") String authorizationValue) {

        if (isEmpty(host) || isEmpty(contextPath) || isEmpty(authorizationHeader) || isEmpty(authorizationValue)) {
            throw new ResourceWebClientInitializeFailedException();
        }

        this.webClient = RemoteWebClient.create(host + contextPath, RemoteWebClient.DefaultHeader.of(authorizationHeader, isEmpty(authorizationPrefix) ? authorizationValue : String.format("%s %s", authorizationPrefix, authorizationValue)));
    }

    public ResourceRemoteResponseBody<MemberVerificationRemoteResponse> verification(String email, String password) {
        return post("/members/verification", MemberVerificationRequest.of(email, password), new ParameterizedTypeReference<>() { });
    }

    private <B, R> R post(String uri, B body, ParameterizedTypeReference<R> parameterizedTypeReference) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path(uri).build())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(parameterizedTypeReference)
                .blockOptional()
                .orElseThrow();
    }
}
