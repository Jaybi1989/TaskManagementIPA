package ch.ipa.jacqueline.backend.repositories;

import ch.ipa.jacqueline.backend.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}