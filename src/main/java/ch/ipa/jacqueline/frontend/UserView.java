package ch.ipa.jacqueline.frontend;

import ch.ipa.jacqueline.backend.entities.Role;
import ch.ipa.jacqueline.backend.entities.User;
import ch.ipa.jacqueline.backend.services.UserService;
import ch.ipa.jacqueline.backend.services.RoleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@PageTitle("Benutzerverwaltung")
@Route(value = "users", layout = MainLayout.class)
@RolesAllowed("ROLE_ADMIN")
@Menu(order = 1, icon = "lumo:user", title = "Benutzerverwaltung")
public class UserView extends VerticalLayout {

    private final UserService userService;
    private final RoleService roleService;
    private final Grid<User> grid = new Grid<>(User.class, false);
    private final TextField filterText = new TextField();
    private final Checkbox showInactive = new Checkbox("Inaktive anzeigen");

    public UserView(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
        setSizeFull();
        configureGrid();
        add(getToolbar(), grid);
        updateList();
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter nach Name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        showInactive.addValueChangeListener(e -> updateList());

        Button addButton = new Button("Neuer Benutzer", VaadinIcon.PLUS.create(), e -> openUserDialog(new User()));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.setTooltipText("Neuen Benutzer erstellen");

        return new HorizontalLayout(filterText, showInactive, addButton);
    }

    private void configureGrid() {
        grid.addColumn(User::getUsername).setHeader("Benutzername").setSortable(true);
        grid.addColumn(u -> u.getRole() != null ? u.getRole().getRoleName() : "-").setHeader("Rolle");
        grid.addColumn(user -> user.isActive() ? "Aktiv" : "Inaktiv").setHeader("Status");

        grid.addComponentColumn(user -> {
            HorizontalLayout layout = new HorizontalLayout();

            Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> openUserDialog(user));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            editBtn.setTooltipText("Benutzer bearbeiten");

            Button toggleBtn = new Button(
                    user.isActive() ? VaadinIcon.BAN.create() : VaadinIcon.CHECK.create(),
                    e -> {
                        userService.deactivateUser(user.getId());
                        updateList();
                        Notification.show(user.isActive() ? "Benutzer deaktiviert." : "Benutzer aktiviert.");
                    }
            );
            toggleBtn.addThemeVariants(user.isActive() ? ButtonVariant.LUMO_ERROR : ButtonVariant.LUMO_SUCCESS);
            toggleBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            toggleBtn.setTooltipText(user.isActive() ? "Benutzer deaktivieren" : "Benutzer aktivieren");

            layout.add(editBtn, toggleBtn);
            return layout;
        }).setHeader("Aktionen").setAutoWidth(true);

        grid.setSizeFull();
    }

    private void openUserDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(user.getId() == null ? "Benutzer erstellen" : "Benutzer bearbeiten");

        TextField username = new TextField("Benutzername");
        username.setValue(user.getUsername() != null ? user.getUsername() : "");

        TextField email = new TextField("E-Mail");
        email.setValue(user.getEmail() != null ? user.getEmail() : "");
        email.setRequired(true);

        PasswordField password = new PasswordField("Passwort");

        Checkbox activeCheckBox = new Checkbox("Aktiv");
        activeCheckBox.setValue(user.getId() == null || user.isActive());

        Select<Role> roleSelect = new Select<>();
        roleSelect.setLabel("Rolle");
        roleSelect.setItems(roleService.findAll());
        roleSelect.setItemLabelGenerator(Role::getRoleName);
        if (user.getRole() != null) roleSelect.setValue(user.getRole());

        Button saveBtn = new Button("Speichern", e -> {
            try {
                if (user.getId() == null && (password.getValue() == null || password.getValue().isEmpty())) {
                    Notification.show("Passwort ist erforderlich.");
                    password.setInvalid(true);
                    return;
                }
                if (roleSelect.getValue() == null) {
                    Notification.show("Eine Rolle ist erforderlich.");
                    roleSelect.setInvalid(true);
                    return;
                }
                user.setUsername(username.getValue());
                user.setEmail(email.getValue());
                user.setRole(roleSelect.getValue());
                user.setActive(activeCheckBox.getValue());
                if (password.getValue() != null && !password.getValue().isEmpty()) {
                    user.setPasswordHash(password.getValue());
                }
                userService.saveUser(user);
                dialog.close();
                updateList();
                Notification.show("Benutzer gespeichert.");
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(new VerticalLayout(username, email, password, roleSelect, activeCheckBox));
        dialog.getFooter().add(new Button("Abbrechen", e -> dialog.close()), saveBtn);
        dialog.open();
    }

    private void updateList() {
        grid.setItems(userService.getAllUser(filterText.getValue(), showInactive.getValue()));
    }
}