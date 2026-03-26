package ch.ipa.jacqueline.services;

import ch.ipa.jacqueline.backend.entities.Role;
import ch.ipa.jacqueline.backend.entities.User;
import ch.ipa.jacqueline.backend.repositories.UserRepository;
import ch.ipa.jacqueline.backend.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User buildUser(Long id, String username, boolean active) {
        Role role = new Role(1L, "ROLE_USER");
        return new User(id, username, username + "@pritz-it.com", "hash", role, active);
    }

    @Test
    @DisplayName("UT-19: Benutzer: Create - Anlage eines neuen Benutzers mit validem Benutzernamen")
    void createUser_WithValidUsername_ReturnsUser() {
        User user = buildUser(null, "jbigler", true);
        when(userRepository.existsByUsername("jbigler")).thenReturn(false);
        when(passwordEncoder.encode("hash")).thenReturn("encodedHash");
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.createUser(user);

        assertThat(result.getUsername()).isEqualTo("jbigler");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("UT-20: Benutzer: Create - Doppelter Benutzername wird abgelehnt")
    void createUser_WithDuplicateUsername_ThrowsException() {
        User user = buildUser(null, "jbigler", true);
        when(userRepository.existsByUsername("jbigler")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Benutzername existiert bereits");
    }

    @Test
    @DisplayName("UT-21: Benutzer: Read - Abruf aktiver Benutzer filtert inaktive heraus")
    void getAllUser_ShowInactiveFalse_ReturnsOnlyActiveUsers() {
        User active = buildUser(1L, "jbigler", true);
        User inactive = buildUser(2L, "inactive", false);
        when(userRepository.findAll()).thenReturn(List.of(active, inactive));

        List<User> result = userService.getAllUser("", false);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("jbigler");
    }

    @Test
    @DisplayName("UT-22: Benutzer: Update - Passwort-Reset aktualisiert den Hash korrekt")
    void updateUser_WithNewPassword_EncodesAndSaves() {
        User existing = buildUser(1L, "jbigler", true);
        User updated = buildUser(1L, "jbigler", true);
        updated.setPasswordHash("newPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedHash");
        when(userRepository.save(existing)).thenReturn(existing);

        userService.updateUser(updated);

        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(existing);
    }

    @Test
    @DisplayName("UT-23: Benutzer: Deaktivieren - Benutzer wird auf inaktiv gesetzt")
    void deactivateUser_SetsActiveFalse_UserDeactivated() {
        User user = buildUser(1L, "jbigler", true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deactivateUser(1L);

        assertThat(user.isActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("UT-24: Benutzer: Aktivieren - Deaktivierter Benutzer wird wieder aktiviert")
    void deactivateUser_WhenInactive_SetsActiveTrue_UserActivated() {
        User user = buildUser(1L, "jbigler", false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deactivateUser(1L);

        assertThat(user.isActive()).isTrue();
        verify(userRepository).save(user);
    }
}