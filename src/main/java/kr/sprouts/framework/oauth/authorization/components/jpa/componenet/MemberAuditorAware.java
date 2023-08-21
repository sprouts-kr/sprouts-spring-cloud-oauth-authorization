package kr.sprouts.framework.oauth.authorization.components.jpa.componenet;

import kr.sprouts.framework.oauth.authorization.components.utility.PatternMatcher;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class MemberAuditorAware implements AuditorAware<UUID> {
    @Override
    @NonNull
    public Optional<UUID> getCurrentAuditor() {
        Optional<Authentication> optionalAuthentication = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());

        AtomicReference<String> username = new AtomicReference<>();

        optionalAuthentication.ifPresent(authentication -> {
            if (authentication.isAuthenticated() && authentication.getPrincipal() instanceof User) {
                username.set(((User) authentication.getPrincipal()).getUsername());
            }
        });

        return (username.get() != null && PatternMatcher.uuid(username.get())) ?
                Optional.of(UUID.fromString(username.get())) : Optional.empty();
    }
}
