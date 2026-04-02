package ch.ipa.jacqueline.backend.repositories;

import ch.ipa.jacqueline.backend.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    java.util.List<Project> findByCustomerId(Long customerId);
    long countByCustomerId(long customerId);

    @Query(value = """
        SELECT * FROM project
        WHERE (:showArchived = true OR is_deleted = false)
        AND (
            :filter = '' OR
            LOWER(name) LIKE LOWER(CONCAT('%', :filter, '%'))
        )
        """, nativeQuery = true)
    List<Project> findAllWithFilterNative(
            @Param("filter") String filter,
            @Param("showArchived") boolean showArchived
    );

    @Modifying
    @Query(value = "DELETE FROM project WHERE id = :id", nativeQuery = true)
    void hardDelete(@Param("id") long id);

    @Query(value = "SELECT * FROM project WHERE id = :id", nativeQuery = true)
    Optional<Project> findByIdIgnoreDeleted(@Param("id") Long id);
}