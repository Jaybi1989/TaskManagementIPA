package ch.ipa.jacqueline.frontend;

import ch.ipa.jacqueline.backend.entities.*;
import ch.ipa.jacqueline.backend.services.CustomerService;
import ch.ipa.jacqueline.backend.services.ProjectService;
import ch.ipa.jacqueline.backend.services.TaskService;
import ch.ipa.jacqueline.backend.services.UserService;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Route(value = "projects", layout = MainLayout.class)
@PageTitle("Projektverwaltung")
@RolesAllowed({"ROLE_USER", "ROLE_ADMIN"})
@Menu(order = 3, icon = "lumo:user", title = "Projektverwaltung")
public class ProjectManagementView extends VerticalLayout {

    private final ProjectService projectService;
    private final TaskService taskService;
    private final CustomerService customerService;
    private final UserService userService;

    private final Grid<Project> projectGrid = new Grid<>(Project.class, false);
    private final Grid<Task> taskGrid = new Grid<>(Task.class, false);

    private final TextField filterField = new TextField();
    private final Checkbox showArchived = new Checkbox("Archivierte anzeigen");

    private Project selectedProject;

    public ProjectManagementView(ProjectService projectService, TaskService taskService,
                                 CustomerService customerService, UserService userService) {
        this.projectService = projectService;
        this.taskService = taskService;
        this.customerService = customerService;
        this.userService = userService;

        addClassName("project-management-view");
        setSizeFull();

        configureProjectGrid();
        configureTaskGrid();

        add(getToolbar(), getMainContent());
        refreshData();
    }

    private HorizontalLayout getToolbar() {
        filterField.setPlaceholder("Suche...");
        filterField.setClearButtonVisible(true);
        filterField.setValueChangeMode(ValueChangeMode.LAZY);
        filterField.addValueChangeListener(e -> refreshData());

        showArchived.addValueChangeListener(e -> refreshData());

        Button addProjectBtn = new Button("Neues Projekt", VaadinIcon.PLUS.create(),
                e -> openProjectDialog(new Project()));
        addProjectBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addProjectBtn.setTooltipText("Neues Projekt erstellen");

        HorizontalLayout toolbar = new HorizontalLayout(filterField, showArchived, addProjectBtn);
        toolbar.setVerticalComponentAlignment(Alignment.END, showArchived);
        return toolbar;
    }

    private HorizontalLayout getMainContent() {
        HorizontalLayout content = new HorizontalLayout(projectGrid, taskGrid);
        content.setFlexGrow(1, projectGrid);
        content.setFlexGrow(2, taskGrid);
        content.setSizeFull();
        return content;
    }

    private void configureProjectGrid() {
        projectGrid.setSizeFull();
        projectGrid.addColumn(Project::getName).setHeader("Projekt").setSortable(true).setAutoWidth(true);
        projectGrid.addColumn(p -> p.getCustomer() != null ? p.getCustomer().getName() : "-").setHeader("Kunde").setAutoWidth(true);

        projectGrid.addComponentColumn(p -> {
            HorizontalLayout actions = new HorizontalLayout();

            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!p.isDeleted()) {
                Button editBtn = new Button(VaadinIcon.EDIT.create(),
                        e -> projectService.findById(p.getId()).ifPresent(this::openProjectDialog));
                editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
                editBtn.setTooltipText("Projekt bearbeiten");

                Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
                    projectService.deleteProject(p.getId());
                    refreshData();
                    Notification.show("Projekt archiviert.");
                });
                deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
                deleteBtn.setTooltipText("Projekt archivieren");

                actions.add(editBtn, deleteBtn);
            } else if (isAdmin) {
                Button hardDeleteBtn = new Button(VaadinIcon.ERASER.create(),
                        e -> openConfirmDeleteDialog(p));
                hardDeleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
                hardDeleteBtn.setTooltipText("Projekt dauerhaft löschen");
                actions.add(hardDeleteBtn);
            }
            return actions;
        }).setHeader("Aktionen").setWidth("160px").setFlexGrow(0);

        projectGrid.asSingleSelect().addValueChangeListener(e -> {
            selectedProject = e.getValue();
            updateTaskList();
        });
    }

    private void openConfirmDeleteDialog(Project project) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Dauerhaft löschen?");

        VerticalLayout content = new VerticalLayout(
                new Html("<p>Möchten Sie das Projekt <b>" + project.getName() + "</b> wirklich dauerhaft löschen?</p>"),
                new Span("Dieser Vorgang kann nicht rückgängig gemacht werden.")
        );
        content.setPadding(false);

        Button deleteBtn = new Button("Dauerhaft löschen", VaadinIcon.ERASER.create(), e -> {
            projectService.hardDeleteProject(project.getId());
            refreshData();
            confirmDialog.close();
            Notification.show("Projekt dauerhaft gelöscht.");
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        deleteBtn.setTooltipText("Projekt dauerhaft aus der Datenbank löschen");

        confirmDialog.add(content);
        confirmDialog.getFooter().add(new Button("Abbrechen", e -> confirmDialog.close()), deleteBtn);
        confirmDialog.open();
    }

    private void configureTaskGrid() {
        taskGrid.setSizeFull();
        taskGrid.addColumn(Task::getTitle).setHeader("Aufgabe").setSortable(true);
        taskGrid.addColumn(t -> t.getStatus() != null ? t.getStatus().getLabel() : "-").setHeader("Status");
        taskGrid.addColumn(t -> t.getPriority() != null ? t.getPriority().getLabel() : "-").setHeader("Prio");

        taskGrid.addColumn(t -> t.getDueDate() != null ?
                t.getDueDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "-"
        ).setHeader("Fällig");

        taskGrid.addColumn(t -> t.getAssignedUser() != null ?
                t.getAssignedUser().getUsername() : "Unzugewiesen"
        ).setHeader("Bearbeiter");

        taskGrid.addComponentColumn(t -> {
            HorizontalLayout actions = new HorizontalLayout();

            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!t.isDeleted()) {
                Button editBtn = new Button(VaadinIcon.EDIT.create(),
                        e -> taskService.findById(t.getId()).ifPresent(this::openTaskDialog));
                editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
                editBtn.setTooltipText("Aufgabe bearbeiten");

                Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
                    taskService.deleteTask(t.getId());
                    updateTaskList();
                    Notification.show("Aufgabe archiviert.");
                });
                deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
                deleteBtn.setTooltipText("Aufgabe archivieren");

                actions.add(editBtn, deleteBtn);
            } else if (isAdmin) {
                Button hardDeleteBtn = new Button(VaadinIcon.ERASER.create(),
                        e -> openConfirmTaskDeleteDialog(t));
                hardDeleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
                hardDeleteBtn.setTooltipText("Aufgabe dauerhaft löschen");
                actions.add(hardDeleteBtn);
            }
            return actions;
        }).setHeader("Aktionen").setAutoWidth(true);

        Button addTaskBtn = new Button("Aufgabe hinzufügen", VaadinIcon.PLUS.create(),
                e -> openTaskDialog(new Task()));
        addTaskBtn.setEnabled(false);
        addTaskBtn.setTooltipText("Neue Aufgabe zum ausgewählten Projekt hinzufügen");

        projectGrid.asSingleSelect().addValueChangeListener(e -> {
            Project selected = e.getValue();
            addTaskBtn.setEnabled(selected != null && !selected.isDeleted());
        });

        taskGrid.prependHeaderRow().getCells().getFirst().setComponent(addTaskBtn);
    }

    private void openConfirmTaskDeleteDialog(Task task) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Dauerhaft löschen?");

        VerticalLayout content = new VerticalLayout(
                new Html("<p>Möchten Sie die Aufgabe <b>" + task.getTitle() + "</b> wirklich dauerhaft löschen?</p>"),
                new Span("Dieser Vorgang kann nicht rückgängig gemacht werden.")
        );
        content.setPadding(false);

        Button deleteBtn = new Button("Dauerhaft löschen", VaadinIcon.ERASER.create(), e -> {
            taskService.hardDeleteTask(task.getId());
            updateTaskList();
            confirmDialog.close();
            Notification.show("Aufgabe dauerhaft gelöscht.");
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        deleteBtn.setTooltipText("Aufgabe dauerhaft aus der Datenbank löschen");

        confirmDialog.add(content);
        confirmDialog.getFooter().add(new Button("Abbrechen", e -> confirmDialog.close()), deleteBtn);
        confirmDialog.open();
    }

    private void refreshData() {
        updateProjectList();
        updateTaskList();
    }

    private void updateProjectList() {
        projectGrid.setItems(projectService.findAllProjects(filterField.getValue(), showArchived.getValue()));
    }

    private void updateTaskList() {
        List<Task> tasks;
        if (selectedProject != null) {
            tasks = taskService.findByProject(selectedProject.getId(), showArchived.getValue());
        } else {
            tasks = taskService.findAllTasks(filterField.getValue(), showArchived.getValue());
        }
        taskGrid.setItems(tasks);
    }

    private void openProjectDialog(Project project) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(project.getId() == null ? "Neues Projekt" : "Projekt bearbeiten");

        TextField name = new TextField("Name");
        TextArea description = new TextArea("Beschreibung");
        ComboBox<Customer> customer = new ComboBox<>("Kunde");
        customer.setItems(customerService.findAllActiveCustomers(""));
        customer.setItemLabelGenerator(Customer::getName);
        customer.setRequiredIndicatorVisible(true);

        BeanValidationBinder<Project> binder = new BeanValidationBinder<>(Project.class);
        binder.bind(name, "name");
        binder.bind(description, "description");
        binder.forField(customer)
                .asRequired("Ein Kunde muss zugewiesen werden")
                .bind(Project::getCustomer, Project::setCustomer);
        binder.setBean(project);

        Button saveBtn = new Button("Speichern", e -> {
            try {
                if (binder.validate().isOk()) {
                    projectService.saveProject(project);
                    Notification.show(project.getId() == null ? "Projekt erstellt." : "Projekt aktualisiert.");
                    refreshData();
                    dialog.close();
                }
            } catch (Exception ex) {
                log.error("Fehler beim Speichern des Projekts: {}", ex.getMessage());
                Notification.show("Fehler: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        VerticalLayout layout = new VerticalLayout(name, description, customer);
        layout.setPadding(false);
        dialog.add(layout);
        dialog.getFooter().add(new Button("Abbrechen", i -> dialog.close()), saveBtn);
        dialog.open();
    }

    private void openTaskDialog(Task task) {
        if (selectedProject == null && task.getId() == null) return;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(task.getId() == null ? "Neue Aufgabe" : "Aufgabe bearbeiten");

        TextField title = new TextField("Titel");
        TextArea description = new TextArea("Beschreibung");

        ComboBox<TaskStatus> status = new ComboBox<>("Status");
        status.setItems(TaskStatus.values());
        status.setItemLabelGenerator(TaskStatus::getLabel);

        ComboBox<TaskPriority> priority = new ComboBox<>("Priorität");
        priority.setItems(TaskPriority.values());
        priority.setItemLabelGenerator(TaskPriority::getLabel);

        DatePicker dueDate = new DatePicker("Fälligkeit");
        dueDate.setMin(java.time.LocalDate.now());

        ComboBox<User> assignedUser = new ComboBox<>("Bearbeiter");
        assignedUser.setItems(userService.getAllUser("", false));
        assignedUser.setItemLabelGenerator(User::getUsername);

        BeanValidationBinder<Task> binder = new BeanValidationBinder<>(Task.class);
        binder.forField(dueDate)
                .withConverter(d -> d != null ? d.atStartOfDay() : null, t -> t != null ? t.toLocalDate() : null)
                .bind(Task::getDueDate, Task::setDueDate);
        binder.forField(title)
                .asRequired("Titel ist erforderlich")
                .bind(Task::getTitle, Task::setTitle);
        binder.bind(description, "description");
        binder.bind(status, "status");
        binder.bind(priority, "priority");
        binder.bind(assignedUser, "assignedUser");
        binder.setBean(task);

        Button saveBtn = new Button("Speichern", e -> {
            try {
                if (binder.writeBeanIfValid(task)) {
                    if (task.getProject() == null) {
                        task.setProject(selectedProject);
                    }
                    taskService.saveTask(task);
                    Notification.show(task.getId() == null ? "Aufgabe erstellt." : "Aufgabe aktualisiert.");
                    updateTaskList();
                    dialog.close();
                }
            } catch (Exception ex) {
                log.error("Fehler beim Speichern der Aufgabe: {}", ex.getMessage());
                Notification.show("Fehler: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(new VerticalLayout(title, description, status, priority, dueDate, assignedUser));
        dialog.getFooter().add(new Button("Abbrechen", i -> dialog.close()), saveBtn);
        dialog.open();
    }
}