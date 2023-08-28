package kr.sprouts.framework.service.oauth.authorization.applications.authorize.controller;

import kr.sprouts.framework.autoconfigure.web.response.components.entity.StructuredResponse;
import kr.sprouts.framework.autoconfigure.web.response.components.entity.StructuredResponseEntity;
import kr.sprouts.framework.service.oauth.authorization.applications.authorize.dto.request.CredentialRequest;
import kr.sprouts.framework.service.oauth.authorization.applications.authorize.dto.response.CredentialResponse;
import kr.sprouts.framework.service.oauth.authorization.applications.authorize.service.AuthorizeRemoteService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(value = AuthorizeController.REQUEST_PATH)
public class AuthorizeController {
    static final String REQUEST_PATH = "authorize";
    private final AuthorizeRemoteService authorizeRemoteService;

    public AuthorizeController(AuthorizeRemoteService authorizeRemoteService) {
        this.authorizeRemoteService = authorizeRemoteService;
    }

    @PostMapping(value = "/providers/{providerId}/credential")
    public StructuredResponseEntity credential(@PathVariable(value = "providerId") UUID providerId, @RequestBody CredentialRequest request) {
        return StructuredResponse.succeeded(CredentialResponse.fromProxy(
                authorizeRemoteService.verification(providerId, request.getEmail(), request.getPassword())
        ));
    }
}
