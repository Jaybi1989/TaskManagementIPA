package ch.ipa.jacqueline.backend.services;

import ch.ipa.jacqueline.backend.entities.Role;
import ch.ipa.jacqueline.backend.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public List<Role> findAll() {
        return roleRepository.findAll();
    }
}



