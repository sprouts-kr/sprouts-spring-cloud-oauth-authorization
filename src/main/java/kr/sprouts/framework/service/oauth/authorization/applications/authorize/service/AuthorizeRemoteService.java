package kr.sprouts.framework.service.oauth.authorization.applications.authorize.service;

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
import kr.sprouts.framework.service.oauth.authorization.applications.authorize.dto.proxy.AuthorizationProxy;
import kr.sprouts.framework.service.oauth.authorization.applications.authorize.dto.proxy.CredentialProxy;
import kr.sprouts.framework.service.oauth.authorization.applications.authorize.dto.proxy.MemberProxy;
import kr.sprouts.framework.service.oauth.authorization.applications.authorize.exception.AuthorizeRemoteServiceInitializeFailedException;
import kr.sprouts.framework.service.oauth.authorization.applications.authorize.exception.UnAuthorizedException;
import kr.sprouts.framework.service.oauth.authorization.applications.remote.resource.ResourceRemoteClient;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Service
@Transactional(readOnly = true)
public class AuthorizeRemoteService {
    private final ResourceRemoteClient resourceRemoteClient;
    private final CredentialProviderManager credentialProviderManager;
    private final CredentialHeaderSpec credentialHeaderSpec;
    private final Codec codec;

    public AuthorizeRemoteService(ResourceRemoteClient resourceRemoteClient,
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
        AtomicReference<MemberProxy> memberProxyAtomicReference = new AtomicReference<>();

        Optional.ofNullable(resourceRemoteClient.verification(email, password).getContent()).ifPresent(verification -> {
            if (Boolean.FALSE.equals(verification.isVerification()) || verification.getMember() == null || verification.getMember().getId() == null) {
                throw new UnAuthorizedException();
            }
            memberProxyAtomicReference.set(MemberProxy.fromRemoteResponse(verification.getMember()));
        });

        MemberProxy member = memberProxyAtomicReference.get();

        AtomicReference<Credential> credentialAtomicReference = new AtomicReference<>();

        credentialProviderManager.get(providerId).ifPresent(credentialProvider -> {
            if (credentialProvider instanceof ApiKeyCredentialProvider) {
                credentialAtomicReference.set(((ApiKeyCredentialProvider)credentialProvider).provide(ApiKeySubject.of(member.getId())));
            } else if (credentialProvider instanceof BearerTokenCredentialProvider) {
                credentialAtomicReference.set(((BearerTokenCredentialProvider) credentialProvider).provide(BearerTokenSubject.of(member.getId(), 1L)));
            }
        });

        if (credentialAtomicReference.get() == null) {
            throw new UnAuthorizedException();
        }

        AuthorizationProxy authorizationProxy = AuthorizationProxy.of(credentialHeaderSpec.getName(), isEmpty(credentialHeaderSpec.getPrefix()) ? codec.encodeToString(SerializationUtils.serialize(credentialAtomicReference.get())) : String.format("%s %s", credentialHeaderSpec.getPrefix(), codec.encodeToString(SerializationUtils.serialize(credentialAtomicReference.get()))));

        return CredentialProxy.of(authorizationProxy, member);
    }
}
