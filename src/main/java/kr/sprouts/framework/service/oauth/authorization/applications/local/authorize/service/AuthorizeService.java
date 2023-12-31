package kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.service;

import kr.sprouts.framework.autoconfigure.security.credential.consumer.components.BearerTokenCredentialConsumer;
import kr.sprouts.framework.autoconfigure.security.credential.consumer.components.CredentialConsumerManager;
import kr.sprouts.framework.autoconfigure.security.credential.provider.components.BearerTokenCredentialProvider;
import kr.sprouts.framework.autoconfigure.security.credential.provider.components.BearerTokenSubject;
import kr.sprouts.framework.autoconfigure.security.credential.provider.components.CredentialProviderManager;
import kr.sprouts.framework.autoconfigure.security.credential.provider.properties.CredentialProviderConfigurationProperty;
import kr.sprouts.framework.library.security.credential.Credential;
import kr.sprouts.framework.library.security.credential.CredentialHeaderSpec;
import kr.sprouts.framework.library.security.credential.codec.Codec;
import kr.sprouts.framework.library.security.credential.codec.CodecType;
import kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.dto.proxy.AuthorizeProxy;
import kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.dto.proxy.BearerTokenProxy;
import kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.dto.proxy.MemberProxy;
import kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.exception.commit.UnAuthorizedException;
import kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.exception.rollback.CredentialProviderNotFoundException;
import kr.sprouts.framework.service.oauth.authorization.applications.local.authorize.exception.rollback.UnsupportedCredentialProviderException;
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
    private final CredentialConsumerManager credentialConsumerManager;
    private final CredentialHeaderSpec credentialHeaderSpec;
    private final Codec codec;
    private final UUID accessTokenProviderId;
    private final Long accessTokenValidityInMinutes;
    private final UUID refreshTokenProviderId;
    private final Long refreshTokenValidityInMinutes;

    public AuthorizeService(ResourceRemoteClient resourceRemoteClient,
                            CredentialProviderManager credentialProviderManager,
                            CredentialConsumerManager credentialConsumerManager, CredentialProviderConfigurationProperty credentialProviderConfigurationProperty,
                            @Value("${sprouts.application.local.authorize.accessToken.providerId}") UUID accessTokenProviderId,
                            @Value("${sprouts.application.local.authorize.accessToken.validityInMinutes}") Long accessTokenValidityInMinutes,
                            @Value("${sprouts.application.local.authorize.refreshToken.providerId}") UUID refreshTokenProviderId,
                            @Value("${sprouts.application.local.authorize.refreshToken.validityInMinutes}") Long refreshTokenValidityInMinutes) {
        this.resourceRemoteClient = resourceRemoteClient;
        this.credentialProviderManager = credentialProviderManager;
        this.credentialConsumerManager = credentialConsumerManager;
        this.credentialHeaderSpec = credentialProviderConfigurationProperty.getHeader();

        if (this.credentialProviderManager == null
                || this.credentialHeaderSpec == null
                || isEmpty(this.credentialHeaderSpec.getCodec())) {
            throw new ServiceCreateFailedException();
        }

        this.codec = CodecType.fromName(this.credentialHeaderSpec.getCodec()).getCodecSupplier().get();

        if (this.codec == null) {
            throw new ServiceCreateFailedException();
        }

        if (accessTokenProviderId == null
                || accessTokenValidityInMinutes == null
                || refreshTokenProviderId == null
                || refreshTokenValidityInMinutes == null) {
            throw new ServiceCreateFailedException();
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

    public AuthorizeProxy authorize(String email, String password) {
        MemberProxy member = verification(email, password);

        return AuthorizeProxy.of(createAccessToken(member.getId()), createRefreshToken(member.getId()), member);
    }

    public AuthorizeProxy refresh(String authorizationValue) {
        MemberProxy member = verification(authorizationValue);

        return AuthorizeProxy.of(createAccessToken(member.getId()), createRefreshToken(member.getId()), member);
    }

    private BearerTokenProxy createRefreshToken(UUID memberId) {
        return BearerTokenProxy.of(
                credentialHeaderSpec.getName(),
                appendPrefix(bearerToken(refreshTokenProviderId, refreshTokenValidityInMinutes, memberId)),
                refreshTokenValidityInMinutes
        );
    }

    private BearerTokenProxy createAccessToken(UUID memberId) {
        return BearerTokenProxy.of(
                credentialHeaderSpec.getName(),
                appendPrefix(bearerToken(accessTokenProviderId, accessTokenValidityInMinutes, memberId)),
                accessTokenValidityInMinutes
        );
    }

    private MemberProxy verification(String authorizationValue) {
        try {
            if (isEmpty(authorizationValue)
                    || Boolean.FALSE.equals(authorizationValue.startsWith(credentialHeaderSpec.getPrefix())
                    || authorizationValue.trim().length() <= credentialHeaderSpec.getPrefix().length())) {
                throw new UnAuthorizedException();
            }

            Object credentialObject = SerializationUtils.deserialize(
                    codec.decode(authorizationValue.substring(credentialHeaderSpec.getPrefix().length()).trim())
            );

            if (Boolean.FALSE.equals(credentialObject instanceof Credential)) {
                throw new UnAuthorizedException();
            }

            Credential credential = (Credential) credentialObject;

            AtomicReference<BearerTokenCredentialConsumer> credentialConsumerAtomicReference = new AtomicReference<>();

            for (UUID targetConsumerId : credential.getConsumerIds()) {
                credentialConsumerManager.getConsumer(targetConsumerId).ifPresent(credentialConsumer -> {
                    if (credentialConsumer instanceof BearerTokenCredentialConsumer bearerTokenCredentialConsumer) {
                        credentialConsumerAtomicReference.set(bearerTokenCredentialConsumer);
                    }
                });

                if (credentialConsumerAtomicReference.get() != null) break;
            }

            if (credentialConsumerAtomicReference.get() == null) {
                throw new UnAuthorizedException();
            }

            UUID memberId = credentialConsumerAtomicReference.get().consume(credential).getSubject().getMemberId();

            AtomicReference<MemberRemoteResponse> memberRemoteResponseAtomicReference = new AtomicReference<>();

            resourceRemoteClient.getMemberById(memberId).getContent().ifPresentOrElse(content -> {
                if (Boolean.FALSE.equals("ACTIVE".equals(content.getStatus()))) throw new UnAuthorizedException();

                memberRemoteResponseAtomicReference.set(content);
            }, UnAuthorizedException::new);

            return MemberProxy.fromRemoteResponse(memberRemoteResponseAtomicReference.get());
        } catch (RuntimeException e) {
            throw new UnAuthorizedException();
        }
    }

    private MemberProxy verification(String email, String password) {
        try {
            AtomicReference<MemberVerificationRemoteResponse> contentAtomicReference = new AtomicReference<>();

            resourceRemoteClient.verificationMember(email, password).getContent().ifPresentOrElse(content -> {
                if (Boolean.FALSE.equals(content.isVerification())) throw new UnAuthorizedException();

                contentAtomicReference.set(content);
            }, UnAuthorizedException::new);

            AtomicReference<MemberRemoteResponse> memberRemoteResponseAtomicReference = new AtomicReference<>();
            contentAtomicReference.get().getMember().ifPresentOrElse(member -> {
                if (member.getId() == null) throw new UnAuthorizedException();

                memberRemoteResponseAtomicReference.set(member);
            }, UnAuthorizedException::new);

            return MemberProxy.fromRemoteResponse(memberRemoteResponseAtomicReference.get());
        } catch (RuntimeException e) {
            throw new UnAuthorizedException();
        }
    }

    private String bearerToken(UUID providerId, Long validityInMinutes, UUID memberId) {
        AtomicReference<BearerTokenCredentialProvider> credentialProviderAtomicReference = new AtomicReference<>();

        credentialProviderManager.getProvider(providerId).ifPresentOrElse(credentialProvider -> {
            if (Boolean.FALSE.equals(credentialProvider instanceof BearerTokenCredentialProvider)) {
                throw new UnsupportedCredentialProviderException();
            }

            credentialProviderAtomicReference.set((BearerTokenCredentialProvider) credentialProvider);
        }, CredentialProviderNotFoundException::new);

        return codec.encodeToString(SerializationUtils.serialize(
                credentialProviderAtomicReference.get().provide(BearerTokenSubject.of(memberId, validityInMinutes))
        ));
    }

    private String appendPrefix(String authorizationValue) {
        return isEmpty(credentialHeaderSpec.getPrefix()) ?
                authorizationValue : String.format("%s %s", credentialHeaderSpec.getPrefix(), authorizationValue);
    }

    private static class ServiceCreateFailedException extends RuntimeException { }
}
