package kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.controller;

import kr.sprouts.framework.autoconfigure.web.response.components.entity.StructuredResponse;
import kr.sprouts.framework.autoconfigure.web.response.components.entity.StructuredResponseEntity;
import kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.dto.request.AuthorizeRequest;
import kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.dto.request.CredentialRequest;
import kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.dto.response.AuthorizeResponse;
import kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.dto.response.CredentialResponse;
import kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.service.AuthorizeService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(value = AuthorizeController.REQUEST_PATH)
public class AuthorizeController {
    static final String REQUEST_PATH = "authorize";
    private final AuthorizeService authorizeService;

    public AuthorizeController(AuthorizeService authorizeService) {
        this.authorizeService = authorizeService;
    }

    @PostMapping
    public StructuredResponseEntity authorize(@RequestBody AuthorizeRequest request) {
        return StructuredResponse.succeeded(AuthorizeResponse.fromProxy(
                authorizeService.authorize(request.getEmail(), request.getPassword())
        ));
    }

    @PostMapping(value = "/refresh")
    public StructuredResponseEntity refresh(@RequestHeader(value = "Authorization") String authorization) {
        return StructuredResponse.succeeded(AuthorizeResponse.fromProxy(
                authorizeService.refresh(authorization)
        ));
    }

    @PostMapping(value = "/providers/{providerId}/credential")
    public StructuredResponseEntity credential(@PathVariable(value = "providerId") UUID providerId, @RequestBody CredentialRequest request) {
        return StructuredResponse.succeeded(CredentialResponse.fromProxy(
                authorizeService.verification(providerId, request.getEmail(), request.getPassword())
        ));
    }
}
