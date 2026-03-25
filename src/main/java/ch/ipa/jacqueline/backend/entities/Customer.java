package ch.ipa.jacqueline.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "customers")
@SQLDelete(sql = "UPDATE customers SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_generator")
    @SequenceGenerator(
            name = "customer_generator",
            sequenceName = "customers_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 100)
    private String email;

    @Column(nullable = false)
    private String address;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;
}