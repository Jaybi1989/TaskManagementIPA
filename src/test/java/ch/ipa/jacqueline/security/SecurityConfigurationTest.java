package ch.ipa.jacqueline.security;

import ch.ipa.jacqueline.backend.entities.Role;
import ch.ipa.jacqueline.backend.entities.User;
import ch.ipa.jacqueline.backend.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigurationTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("UT-17: Security: Unauthentifizierter Zugriff wird abgefangen")
    void unknownUser_ThrowsUsernameNotFoundException() {
        when(userRepository.findByUsername("unbekannt")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unbekannt"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Benutzer nicht gefunden");
    }

    @Test
    @DisplayName("UT-18: Security: ROLE_USER kann keine Admin-Operation ausführen")
    void roleUser_HasNoAdminAuthority() {
        Role userRole = new Role(null, "ROLE_USER");
        User user = new User(null, "user", "user@pritz-it.ch",
                "$2a$10$ToT1.ZH1UZR.gFrSJx44perQx2mI5cfJ/Pw75kkBwA/kHNkK6/hCi",
                userRole, true);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("user");

        assertThat(details.getAuthorities())
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        assertThat(details.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    }
}