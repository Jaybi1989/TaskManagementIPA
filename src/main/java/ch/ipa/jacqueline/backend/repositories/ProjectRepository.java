package ch.ipa.jacqueline.backend.repositories;

import ch.ipa.jacqueline.backend.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    java.util.List<Project> findByCustomerId(Long customerId);
}