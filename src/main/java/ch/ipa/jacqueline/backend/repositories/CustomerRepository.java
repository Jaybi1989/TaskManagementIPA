package ch.ipa.jacqueline.backend.repositories;

import ch.ipa.jacqueline.backend.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query(value = """
            SELECT * FROM customers
            WHERE (:showArchived = true OR is_deleted = false)
            AND (
                :filter = '' OR
                LOWER(name) LIKE LOWER(CONCAT('%', :filter, '%')) OR
                LOWER(email) LIKE LOWER(CONCAT('%', :filter, '%'))
            )
            """, nativeQuery = true)
    List<Customer> findAllWithFilterNative(
            @Param("filter") String filter,
            @Param("showArchived") boolean showArchived
    );

    @Query(value = "SELECT * FROM customers WHERE id = :id", nativeQuery = true)
    Optional<Customer> findByIdIgnoreDeleted(@Param("id") Long id);
}