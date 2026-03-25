package ch.ipa.jacqueline.backend.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "users",
        indexes = @Index(name = "idx_user_username", columnList = "username")
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_generator")
    @SequenceGenerator(
            name = "user_generator",
            sequenceName = "users_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}