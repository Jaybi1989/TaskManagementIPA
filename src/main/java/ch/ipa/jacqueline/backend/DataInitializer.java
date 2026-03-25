package ch.ipa.jacqueline.backend;

import ch.ipa.jacqueline.backend.entities.Role;
import ch.ipa.jacqueline.backend.entities.User;
import ch.ipa.jacqueline.backend.repositories.RoleRepository;
import ch.ipa.jacqueline.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {

        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(null, "ROLE_ADMIN"));
            roleRepository.save(new Role(null, "ROLE_USER"));
        }

        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByRoleName("ROLE_ADMIN").orElseThrow();
            Role userRole  = roleRepository.findByRoleName("ROLE_USER").orElseThrow();

            userRepository.save(new User(
                    null,
                    "admin",
                    "admin@pritz-it.ch",
                    "$2a$10$Dp.abGde40kwozVc0UOkR.GC9O/y4GPvbE7TBg8FNGM/zeOTICZTu",
                    adminRole,
                    true
            ));

            userRepository.save(new User(
                    null,
                    "user",
                    "user@pritz-it.ch",
                    "$2a$10$ToT1.ZH1UZR.gFrSJx44perQx2mI5cfJ/Pw75kkBwA/kHNkK6/hCi",
                    userRole,
                    true
            ));
        }
    }
}