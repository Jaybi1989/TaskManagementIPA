package ch.ipa.jacqueline.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "project", indexes = @Index(name = "idx_customer_id", columnList = "customer_id"))
@SQLDelete(sql = "UPDATE project SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Project extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_generator")
    @SequenceGenerator(
            name = "project_generator",
            sequenceName = "project_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;
}