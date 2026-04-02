package ch.ipa.jacqueline.security;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {

    private final Long id;

    public CustomUserDetails(Long id, String username, String password,
                             boolean enabled,
                             Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, true, true, true, authorities);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}