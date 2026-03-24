package ch.ipa.jacqueline.backend.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_generator")
    @SequenceGenerator(
            name = "role_generator",
            sequenceName = "roles_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "role_name", unique = true, nullable = false, length = 50)
    private String roleName;
}