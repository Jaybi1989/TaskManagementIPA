package ch.ipa.jacqueline.security;

import ch.ipa.jacqueline.backend.entities.User;
import ch.ipa.jacqueline.backend.repositories.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden: " + username));

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                formatPasswordHash(user.getPasswordHash()),
                user.isActive(),
                mapRolesToAuthorities(user)
        );
    }

    private String formatPasswordHash(String passwordHash) {
        if (passwordHash == null) return "";
        return passwordHash.replaceAll("[\\p{Z}\\s]", "").trim();
    }

    private List<GrantedAuthority> mapRolesToAuthorities(User user) {
        if (user.getRole() == null) return Collections.emptyList();

        String roleName = user.getRole().getRoleName().toUpperCase();
        String securityRole = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;

        return Collections.singletonList(new SimpleGrantedAuthority(securityRole));
    }
}