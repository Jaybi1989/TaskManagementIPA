package ch.ipa.jacqueline.backend.services;

import ch.ipa.jacqueline.backend.entities.*;
import ch.ipa.jacqueline.backend.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public List<Task> findByProject(long projectId, boolean showArchived) {
        return taskRepository.findAllByProjectIdWithArchived(projectId, showArchived);
    }

    public List<Task> findAllTasks(String filterText, boolean showArchived) {
        String filter = (filterText == null) ? "" : filterText;
        return taskRepository.findAllWithFilterNative(filter, showArchived);
    }

    public Optional<Task> findById(long id) {
        return taskRepository.findById(id).filter(t -> !t.isDeleted());
    }

    @Transactional
    public Task saveTask(Task task) {
        if (task.getProject() == null || task.getProject().isDeleted()) {
            throw new IllegalStateException("Task kann nicht gespeichert werden: Projekt ist inaktiv oder fehlt.");
        }
        if (task.getProject().getCustomer() != null && task.getProject().getCustomer().isDeleted()) {
            throw new IllegalStateException("Task kann nicht gespeichert werden: Der zugehörige Kunde ist inaktiv.");
        }
        if (task.getAssignedUser() != null && !task.getAssignedUser().isActive()) {
            throw new IllegalStateException("Task kann nicht einem inaktiven Benutzer zugewiesen werden.");
        }
        if (task.getId() == null) {
            return createTask(task);
        } else {
            return updateTask(task);
        }
    }

    @Transactional
    public Task createTask(Task task) {
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new IllegalArgumentException("Aufgabe muss einen Titel haben.");
        }
        if (task.getStatus() == null) task.setStatus(TaskStatus.OFFEN);
        if (task.getPriority() == null) task.setPriority(TaskPriority.NIEDRIG);
        task.setDeleted(false);
        log.info("INSERT: Neuer Task '{}'", task.getTitle());
        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(Task task) {
        if (!taskRepository.existsById(task.getId())) {
            throw new IllegalArgumentException("Task mit ID " + task.getId() + " existiert nicht.");
        }
        log.info("UPDATE: Task ID {} ('{}')", task.getId(), task.getTitle());
        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(long id) {
        taskRepository.findById(id).ifPresent(task -> {
            task.setDeleted(true);
            taskRepository.save(task);
            log.info("ARCHIVE: Task ID {} archiviert", id);
        });
    }

    @Transactional
    public void restoreTask(long id) {
        taskRepository.findById(id).ifPresent(task -> {
            task.setDeleted(false);
            taskRepository.save(task);
            log.info("RESTORE: Task ID {} wiederhergestellt", id);
        });
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void hardDeleteTask(long id) {
        taskRepository.deleteById(id);
        log.info("HARD DELETE: Task ID {} permanent gelöscht", id);
    }
}