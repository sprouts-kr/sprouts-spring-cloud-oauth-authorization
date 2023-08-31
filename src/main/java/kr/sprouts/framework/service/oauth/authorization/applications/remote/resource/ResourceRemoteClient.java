package kr.sprouts.framework.service.oauth.authorization.applications.remote.resource;

import kr.sprouts.framework.service.oauth.authorization.applications.remote.resource.dto.ResourceRemoteResponseBody;
import kr.sprouts.framework.service.oauth.authorization.applications.remote.resource.dto.request.MemberVerificationRequest;
import kr.sprouts.framework.service.oauth.authorization.applications.remote.resource.dto.response.MemberRemoteResponse;
import kr.sprouts.framework.service.oauth.authorization.applications.remote.resource.dto.response.MemberVerificationRemoteResponse;
import kr.sprouts.framework.service.oauth.authorization.components.webflux.RemoteWebClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
@Slf4j
public class ResourceRemoteClient {
    private final org.springframework.web.reactive.function.client.WebClient webClient;

    public ResourceRemoteClient(@Value("${sprouts.application.remote.resource.host}") String host,
                                @Value("${sprouts.application.remote.resource.contextPath}") String contextPath,
                                @Value("${sprouts.application.remote.resource.authorization.header}") String authorizationHeader,
                                @Value("${sprouts.application.remote.resource.authorization.prefix}") String authorizationPrefix,
                                @Value("${sprouts.application.remote.resource.authorization.value}") String authorizationValue) {

        if (isEmpty(host) || isEmpty(contextPath) || isEmpty(authorizationHeader) || isEmpty(authorizationValue)) {
            throw new RemoteClientCreateFailedException();
        }

        String baseUrl = host + contextPath;

        this.webClient = RemoteWebClient.create(
                baseUrl,
                RemoteWebClient.DefaultHeader.of(
                        authorizationHeader,
                        isEmpty(authorizationPrefix) ? authorizationValue : String.format("%s %s", authorizationPrefix, authorizationValue))
        );
    }

    public ResourceRemoteResponseBody<MemberRemoteResponse> getMemberById(UUID memberId) {
        return get(String.format("/members/%s", memberId), new ParameterizedTypeReference<>() { });
    }

    public ResourceRemoteResponseBody<MemberVerificationRemoteResponse> verificationMember(String email, String password) {
        return post("/members/verification", MemberVerificationRequest.of(email, password), new ParameterizedTypeReference<>() { });
    }

    private <R> R get(String uri, ParameterizedTypeReference<R> parameterizedTypeReference) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(uri).build())
                .retrieve()
                .bodyToMono(parameterizedTypeReference)
                .blockOptional()
                .orElseThrow();
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

    private static class RemoteClientCreateFailedException extends RuntimeException { }
}
