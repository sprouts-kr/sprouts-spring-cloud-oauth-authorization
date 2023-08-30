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
import kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.dto.proxy.AccessTokenProxy;
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
import org.springframework.beans.factory.annotation.Value;
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
    private final UUID accessTokenProviderId;
    private final Long accessTokenValidityInMinutes;
    private final UUID refreshTokenProviderId;
    private final Long refreshTokenValidityInMinutes;

    public AuthorizeService(ResourceRemoteClient resourceRemoteClient,
                            CredentialProviderManager credentialProviderManager,
                            CredentialProviderConfigurationProperty credentialProviderConfigurationProperty,
                            @Value("${local.authorize.accessToken.providerId}") UUID accessTokenProviderId,
                            @Value("${local.authorize.accessToken.validityInMinutes}") Long accessTokenValidityInMinutes,
                            @Value("${local.authorize.refreshToken.providerId}") UUID refreshTokenProviderId,
                            @Value("${local.authorize.refreshToken.validityInMinutes}") Long refreshTokenValidityInMinutes) {
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

        if (accessTokenProviderId == null || accessTokenValidityInMinutes == null || refreshTokenProviderId == null || refreshTokenValidityInMinutes == null) {
            throw new AuthorizeRemoteServiceInitializeFailedException();
        }

        this.accessTokenProviderId = accessTokenProviderId;
        this.accessTokenValidityInMinutes = accessTokenValidityInMinutes;
        this.refreshTokenProviderId = refreshTokenProviderId;
        this.refreshTokenValidityInMinutes = refreshTokenValidityInMinutes;

        if (log.isInfoEnabled()) {
            log.info("Initialized access token provider '{}' {} minutes", this.accessTokenProviderId, this.accessTokenValidityInMinutes);
            log.info("Initialized refresh token provider '{}' {} minutes", this.refreshTokenProviderId, this.refreshTokenValidityInMinutes);
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

    public AccessTokenProxy accessToken(String email, String password) {
        MemberRemoteResponse memberRemoteResponse = verification(email, password);

        String encodedCredential = bearerToken(accessTokenProviderId, accessTokenValidityInMinutes, memberRemoteResponse.getId());

        String authorizationValue = isEmpty(credentialHeaderSpec.getPrefix()) ?
                encodedCredential : String.format("%s %s", credentialHeaderSpec.getPrefix(), encodedCredential);

        return AccessTokenProxy.of(
                AuthorizationProxy.of(credentialHeaderSpec.getName(), authorizationValue),
                MemberProxy.fromRemoteResponse(memberRemoteResponse)
        );
    }

    private MemberRemoteResponse verification(String email, String password) {
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

        return memberAtomicReference.get();
    }

    private String apiKey(UUID providerId, UUID memberId) {
        AtomicReference<ApiKeyCredentialProvider> credentialProviderAtomicReference = new AtomicReference<>();

        credentialProviderManager.getProvider(providerId).ifPresentOrElse(credentialProvider -> {
            if (Boolean.FALSE.equals(credentialProvider instanceof ApiKeyCredentialProvider)) {
                throw new RuntimeException("Unsupported credential provider.");
            }

            credentialProviderAtomicReference.set((ApiKeyCredentialProvider) credentialProvider);
        }, () -> { throw new RuntimeException("Credential provider not found."); });

        return codec.encodeToString(SerializationUtils.serialize(credentialProviderAtomicReference.get().provide(ApiKeySubject.of(memberId))));
    }

    private String bearerToken(UUID providerId, Long validityInMinutes, UUID memberId) {
        AtomicReference<BearerTokenCredentialProvider> credentialProviderAtomicReference = new AtomicReference<>();

        credentialProviderManager.getProvider(providerId).ifPresentOrElse(credentialProvider -> {
            if (Boolean.FALSE.equals(credentialProvider instanceof BearerTokenCredentialProvider)) {
                throw new RuntimeException("Unsupported credential provider.");
            }

            credentialProviderAtomicReference.set((BearerTokenCredentialProvider) credentialProvider);
        }, () -> { throw new RuntimeException("Credential provider not found."); });

        return codec.encodeToString(SerializationUtils.serialize(credentialProviderAtomicReference.get().provide(BearerTokenSubject.of(memberId, validityInMinutes))));
    }
}
