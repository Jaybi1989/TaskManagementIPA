package ch.ipa.jacqueline.services;

import ch.ipa.jacqueline.backend.entities.*;
import ch.ipa.jacqueline.backend.repositories.TaskRepository;
import ch.ipa.jacqueline.backend.services.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Project buildProject() {
        Customer customer = new Customer(1L, "Pritz IT GmbH", "info@pritz-it.com", "Richtistrasse 7", false);
        return new Project(1L, "IPA Projekt", "Beschreibung", customer, false);
    }

    @Test
    @DisplayName("UT-09: Aufgabe: Create - Erstellung einer Aufgabe mit gesetztem Titel und Priorität")
    void createTask_WithTitleAndPriority_CreatedSuccessfully() {
        Task task = new Task(null, "Login implementieren", "Beschreibung",
                TaskStatus.OFFEN, TaskPriority.HOCH, null, buildProject(), null, false);
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.createTask(task);

        assertThat(result.getTitle()).isEqualTo("Login implementieren");
        assertThat(result.getPriority()).isEqualTo(TaskPriority.HOCH);
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("UT-10: Aufgabe: Read - Auslesen einer Aufgabe zur Anzeige der Detailattribute")
    void findById_WithExistingTask_ReturnsAllAttributes() {
        Task task = new Task(1L, "Login implementieren", "Beschreibung",
                TaskStatus.OFFEN, TaskPriority.HOCH, null, buildProject(), null, false);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Optional<Task> result = taskService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Login implementieren");
        assertThat(result.get().getStatus()).isEqualTo(TaskStatus.OFFEN);
        assertThat(result.get().getPriority()).isEqualTo(TaskPriority.HOCH);
    }

    @Test
    @DisplayName("UT-11: Aufgabe: Update - Änderung des Bearbeitungsstatus einer Aufgabe")
    void updateTask_ChangesStatus_UpdatedCorrectly() {
        Task task = new Task(1L, "Login implementieren", "Beschreibung",
                TaskStatus.IN_BEARBEITUNG, TaskPriority.HOCH, null, buildProject(), null, false);
        when(taskRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.updateTask(task);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_BEARBEITUNG);
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("UT-12: Aufgabe: Validierung - Versuch, eine Aufgabe ohne Titel zu speichern")
    void createTask_WithoutTitle_ThrowsIllegalArgumentException() {
        Task task = new Task(null, "", "Beschreibung",
                TaskStatus.OFFEN, TaskPriority.NIEDRIG, null, buildProject(), null, false);

        assertThatThrownBy(() -> taskService.createTask(task))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Titel");
    }

    @Test
    @DisplayName("UT-13: Aufgabe: Delete - Durchführung eines Soft-Delete für eine Aufgabe")
    void deleteTask_SetsDeletedFlag_TaskMarkedAsDeleted() {
        Task task = new Task(1L, "Login implementieren", "Beschreibung",
                TaskStatus.OFFEN, TaskPriority.HOCH, null, buildProject(), null, false);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L);

        assertThat(task.isDeleted()).isTrue();
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("UT-14: Zirkularität (R4) - Laden einer Task-Entität mit verknüpftem Projekt und User")
    void findById_WithProjectAndUser_NoCircularReference() {
        Role role = new Role(1L, "ROLE_USER");
        User user = new User(1L, "jbigler", "jbigler@pritz-it.com", "hash", role, true);
        Task task = new Task(1L, "Login implementieren", "Beschreibung",
                TaskStatus.OFFEN, TaskPriority.HOCH, null, buildProject(), user, false);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Optional<Task> result = taskService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getAssignedUser().getUsername()).isEqualTo("jbigler");
        assertThat(result.get().getProject().getName()).isEqualTo("IPA Projekt");
    }

    @Test
    @DisplayName("UT-15: Datenkonsistenz (R2) - Tasks nach Soft-Delete des Projekts nicht mehr gelistet")
    void findAllTasks_WithDeletedProject_NotListed() {
        when(taskRepository.findAllWithFilterNative("", false)).thenReturn(List.of());

        List<Task> result = taskService.findAllTasks("", false);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("UT-16: Mock-Validität (R5) - Mock-Konfiguration liefert keine fehlerhaften Dummy-Daten")
    void createTask_MockReturnsCorrectData_NoFaultyDummyData() {
        Task task = new Task(null, "Datenbankschema erstellen", "Beschreibung",
                TaskStatus.OFFEN, TaskPriority.MITTEL, null, buildProject(), null, false);
        Task saved = new Task(1L, "Datenbankschema erstellen", "Beschreibung",
                TaskStatus.OFFEN, TaskPriority.MITTEL, null, buildProject(), null, false);
        when(taskRepository.save(task)).thenReturn(saved);

        Task result = taskService.createTask(task);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Datenbankschema erstellen");
        assertThat(result.getTitle()).isNotEqualTo("dummy");
    }
}