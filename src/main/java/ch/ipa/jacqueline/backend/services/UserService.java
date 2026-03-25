package ch.ipa.jacqueline.backend.services;

import ch.ipa.jacqueline.backend.entities.User;
import ch.ipa.jacqueline.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUser(String filter, boolean showInactive) {
        return userRepository.findAll().stream()
                .filter(user -> showInactive || user.isActive())
                .filter(user -> filter == null || filter.isEmpty() ||
                        user.getUsername().toLowerCase().contains(filter.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Optional<User> findActiveUserById(long id) {
        return userRepository.findById(id)
                .filter(User::isActive);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            log.warn("Benutzername existiert bereits: {}", user.getUsername());
            throw new IllegalArgumentException("Benutzername existiert bereits: " + user.getUsername());
        }
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        log.info("Erstelle neuen Benutzer: {}", user.getUsername());
        return userRepository.save(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden"));
        existingUser.setUsername(user.getUsername());
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            existingUser.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        existingUser.setActive(user.isActive());
        log.info("Aktualisiere Benutzer: {}", user.getUsername());
        return userRepository.save(existingUser);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public User saveUser(User user) {
        if (user.getId() == null) {
            return createUser(user);
        } else {
            return updateUser(user);
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivateUser(long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setActive(false);
            userRepository.save(user);
            log.info("Benutzer deaktiviert: {}", user.getUsername());
        });
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void hardDeleteUser(long id) {
        log.warn("HARD DELETE: Benutzer mit ID {} wird endgültig gelöscht!", id);
        userRepository.deleteById(id);
    }
}