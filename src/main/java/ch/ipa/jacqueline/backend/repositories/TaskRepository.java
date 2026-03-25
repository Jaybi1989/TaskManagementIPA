package ch.ipa.jacqueline.backend.repositories;

import ch.ipa.jacqueline.backend.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    java.util.List<Task> findByProjectId(Long projectId);
    java.util.List<Task> findByAssignedUserId(Long userId);
}