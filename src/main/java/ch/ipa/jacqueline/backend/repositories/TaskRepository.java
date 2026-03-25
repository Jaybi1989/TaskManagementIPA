package ch.ipa.jacqueline.backend.repositories;

import ch.ipa.jacqueline.backend.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByAssignedUserId(Long userId);

    List<Task> findAllByProjectId(long projectId);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.deleted = false")
    List<Task> findActiveByProjectId(@Param("projectId") long projectId);

    @Query("SELECT t FROM Task t WHERE t.deleted = false")
    List<Task> findAllActive();
}