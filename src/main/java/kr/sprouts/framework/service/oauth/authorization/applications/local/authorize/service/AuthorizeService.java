package kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.service;

import kr.sprouts.framework.autoconfigure.security.credential.provider.components.ApiKeyCredentialProvider;
import kr.sprouts.framework.autoconfigure.security.credential.provider.components.ApiKeySubject;
import kr.sprouts.framework.autoconfigure.security.credential.provider.components.BearerTokenCredentialProvider;
import kr.sprouts.framework.autoconfigure.security.credential.provider.components.BearerTokenSubject;
import kr.sprouts.framework.autoconfigure.security.credential.provider.components.CredentialProviderManager;
import kr.sprouts.framework.autoconfigure.security.credential.provider.properties.CredentialProviderConfigurationProperty;
import kr.sprouts.framework.library.security.credential.Credential;
import kr.sprouts.framework.library.security.credential.CredentialHeaderSpec;
import kr.sprouts.framework.library.security.credential.codec.Codec;
import kr.sprouts.framework.library.security.credential.codec.CodecType;
import kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.dto.proxy.AuthorizationProxy;
import kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.dto.proxy.CredentialProxy;
import kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.dto.proxy.MemberProxy;
import kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.exception.AuthorizeRemoteServiceInitializeFailedException;
import kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.exception.UnAuthorizedException;
import kr.sprouts.framework.service.oauth.authorization.applications.remote.resource.ResourceRemoteClient;
import kr.sprouts.framework.service.oauth.authorization.applications.remote.resource.dto.response.MemberRemoteResponse;
import kr.sprouts.framework.service.oauth.authorization.applications.remote.resource.dto.response.MemberVerificationRemoteResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Service
@Transactional(readOnly = true)
@Slf4j
public class AuthorizeService {
    private final ResourceRemoteClient resourceRemoteClient;
    private final CredentialProviderManager credentialProviderManager;
    private final CredentialHeaderSpec credentialHeaderSpec;
    private final Codec codec;

    public AuthorizeService(ResourceRemoteClient resourceRemoteClient,
                            CredentialProviderManager credentialProviderManager,
                            CredentialProviderConfigurationProperty credentialProviderConfigurationProperty) {
        this.resourceRemoteClient = resourceRemoteClient;
        this.credentialProviderManager = credentialProviderManager;
        this.credentialHeaderSpec = credentialProviderConfigurationProperty.getHeader();

        if (this.credentialProviderManager == null || this.credentialHeaderSpec == null || isEmpty(this.credentialHeaderSpec.getCodec())) {
            throw new AuthorizeRemoteServiceInitializeFailedException();
        }

        this.codec = CodecType.fromName(this.credentialHeaderSpec.getCodec()).getCodecSupplier().get();

        if (this.codec == null) {
            throw new AuthorizeRemoteServiceInitializeFailedException();
        }
    }

    public CredentialProxy verification(UUID providerId, String email, String password) {
        AtomicReference<MemberVerificationRemoteResponse> contentAtomicReference = new AtomicReference<>();

        resourceRemoteClient.verification(email, password).getContent().ifPresentOrElse(content -> {
            if (Boolean.FALSE.equals(content.isVerification())) throw new UnAuthorizedException();

            contentAtomicReference.set(content);
        }, UnAuthorizedException::new);

        AtomicReference<MemberRemoteResponse> memberAtomicReference = new AtomicReference<>();

        contentAtomicReference.get().getMember().ifPresentOrElse(member -> {
            if (member.getId() == null) throw new UnAuthorizedException();

            memberAtomicReference.set(member);
        }, UnAuthorizedException::new);

        AtomicReference<Credential> credentialAtomicReference = new AtomicReference<>();

        credentialProviderManager.getProvider(providerId).ifPresentOrElse(credentialProvider -> {
            if (credentialProvider instanceof ApiKeyCredentialProvider) {
                credentialAtomicReference.set(
                        ((ApiKeyCredentialProvider) credentialProvider).provide(ApiKeySubject.of(memberAtomicReference.get().getId()))
                );
            } else if (credentialProvider instanceof BearerTokenCredentialProvider) {
                credentialAtomicReference.set(
                        ((BearerTokenCredentialProvider) credentialProvider).provide(BearerTokenSubject.of(memberAtomicReference.get().getId(), 60L))
                );
            } else {
                throw new UnAuthorizedException();
            }
        }, UnAuthorizedException::new);

        if (credentialAtomicReference.get() == null) {
            throw new UnAuthorizedException();
        }

        String encodedCredential = codec.encodeToString(SerializationUtils.serialize(credentialAtomicReference.get()));

        String authorizationValue = isEmpty(credentialHeaderSpec.getPrefix()) ?
                encodedCredential : String.format("%s %s", credentialHeaderSpec.getPrefix(), encodedCredential);

        return CredentialProxy.of(
                AuthorizationProxy.of(credentialHeaderSpec.getName(), authorizationValue),
                MemberProxy.fromRemoteResponse(memberAtomicReference.get())
        );
    }
}
