package ch.ipa.jacqueline.backend.services;

import ch.ipa.jacqueline.backend.entities.Project;
import ch.ipa.jacqueline.backend.repositories.ProjectRepository;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public Optional<Project> findById(long id) {
        return projectRepository.findById(id)
                .filter(project -> !project.isDeleted());
    }

    public List<Project> findAllProjects(String filterText, boolean showArchived) {
        String filter = (filterText == null) ? "" : filterText;
        return projectRepository.findAllWithFilterNative(filter, showArchived);
    }

    @Transactional
    public Project saveProject(Project project) {
        if (project.getId() == null || project.getId() == 0) {
            return createProject(project);
        } else {
            return updateProject(project);
        }
    }

    @Transactional
    public Project createProject(Project project) {
        if (project.getId() != null && project.getId() != 0) {
            throw new IllegalArgumentException("Neues Projekt darf keine ID haben");
        }
        log.info("INSERT: Neues Projekt '{}'", project.getName());
        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProject(Project project) {
        if (project.getId() == null || project.getId() == 0) {
            throw new IllegalArgumentException("Projekt muss eine gültige ID haben");
        }
        projectRepository.findByIdIgnoreDeleted(project.getId())
                .orElseThrow(() -> new IllegalArgumentException("Projekt mit ID " + project.getId() + " nicht gefunden."));
        log.info("UPDATE: Projekt ID {}", project.getId());
        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(long id) {
        projectRepository.findById(id).ifPresent(project -> {
            project.setDeleted(true);
            projectRepository.save(project);
            log.info("Projekt archiviert: {}", project.getName());
        });
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void hardDeleteProject(long id) {
        taskRepository.findAllByProjectId(id).forEach(taskRepository::delete);
        projectRepository.hardDelete(id);
        log.info("Projekt permanent gelöscht: ID {}", id);
    }

    @Transactional
    public void restoreProject(long id) {
        projectRepository.findByIdIgnoreDeleted(id).ifPresent(project -> {
            project.setDeleted(false);
            projectRepository.save(project);
            log.info("Projekt reaktiviert: {}", project.getName());
        });
    }
}