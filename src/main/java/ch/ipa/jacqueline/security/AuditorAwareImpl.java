package ch.ipa.jacqueline.security;

import ch.ipa.jacqueline.backend.entities.User;
import ch.ipa.jacqueline.backend.repositories.UserRepository;
import lombok.NonNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    @NonNull
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName().equals("anonymousUser")) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            if (userDetails instanceof CustomUserDetails custom) {
                return Optional.of(custom.getId());
            }
        }
        return Optional.empty();
    }
}