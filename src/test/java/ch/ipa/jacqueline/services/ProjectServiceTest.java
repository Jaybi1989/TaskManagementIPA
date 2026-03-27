package ch.ipa.jacqueline.services;

import ch.ipa.jacqueline.backend.entities.Customer;
import ch.ipa.jacqueline.backend.entities.Project;
import ch.ipa.jacqueline.backend.repositories.ProjectRepository;
import ch.ipa.jacqueline.backend.repositories.TaskRepository;
import ch.ipa.jacqueline.backend.services.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    @DisplayName("UT-05: Projekt: Create - Erstellung eines Projekts inklusive korrekter Kunden-Referenz")
    void createProject_WithCustomer_AssignedCorrectly() {
        Customer customer = new Customer(1L, "Pritz IT GmbH", "info@pritz-it.com", "Richtistrasse 7", false);
        Project project = new Project(null, "IPA Projekt", "Beschreibung", customer, false);
        when(projectRepository.save(project)).thenReturn(project);

        Project result = projectService.createProject(project);

        assertThat(result.getCustomer().getName()).isEqualTo("Pritz IT GmbH");
        verify(projectRepository).save(project);
    }

    @Test
    @DisplayName("UT-06: Projekt: Read - Abfrage aller Projekte, die einem bestimmten Kunden gehören")
    void findAllProjects_FilterByCustomer_ReturnsMatchingProjects() {
        Customer customer = new Customer(1L, "Pritz IT GmbH", "info@pritz-it.com", "Richtistrasse 7", false);
        Project project = new Project(1L, "IPA Projekt", "Beschreibung", customer, false);
        when(projectRepository.findAll()).thenReturn(List.of(project));

        List<Project> result = projectService.findAllProjects("", false);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomer().getName()).isEqualTo("Pritz IT GmbH");
    }

    @Test
    @DisplayName("UT-07: Projekt: Update - Aktualisierung des Projektstatus auf einen neuen Wert")
    void updateProject_ChangesName_UpdatedCorrectly() {
        Customer customer = new Customer(1L, "Pritz IT GmbH", "info@pritz-it.com", "Richtistrasse 7", false);
        Project project = new Project(1L, "Neuer Name", "Beschreibung", customer, false);
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(projectRepository.save(project)).thenReturn(project);

        Project result = projectService.updateProject(project);

        assertThat(result.getName()).isEqualTo("Neuer Name");
        verify(projectRepository).save(project);
    }

    @Test
    @DisplayName("UT-08: Projekt: Delete - Hard-Delete eines Projekts durch einen Administrator")
    void hardDeleteProject_RemovesProjectFromDatabase() {
        when(taskRepository.findAllByProjectId(1L)).thenReturn(List.of());

        projectService.hardDeleteProject(1L);

        verify(projectRepository).deleteById(1L);
    }
}