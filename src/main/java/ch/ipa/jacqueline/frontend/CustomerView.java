package ch.ipa.jacqueline.frontend;

import ch.ipa.jacqueline.backend.entities.Customer;
import ch.ipa.jacqueline.backend.services.CustomerService;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@PageTitle("Kundenverwaltung")
@Route(value = "customers", layout = MainLayout.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_USER"})
@Menu(order = 2, icon = "lumo:user", title = "Kundenverwaltung")
public class CustomerView extends VerticalLayout {

    private final CustomerService customerService;
    private final AccessAnnotationChecker accessChecker;
    private final Grid<Customer> grid = new Grid<>(Customer.class, false);
    private final TextField filterText = new TextField();
    private final Checkbox showArchived = new Checkbox("Archivierte anzeigen");

    public CustomerView(CustomerService customerService, AccessAnnotationChecker accessChecker) {
        this.customerService = customerService;
        this.accessChecker = accessChecker;
        setSizeFull();
        configureGrid();
        add(getToolbar(), grid);
        updateList();
    }

    private void configureGrid() {
        grid.addColumn(Customer::getName).setHeader("Kundenname").setSortable(true);
        grid.addColumn(Customer::getEmail).setHeader("E-Mail").setSortable(true);
        grid.addColumn(Customer::getAddress).setHeader("Adresse").setSortable(true);

        grid.addComponentColumn(customer -> {
            HorizontalLayout layout = new HorizontalLayout();

            if (!customer.isDeleted()) {
                Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> openCustomerDialog(customer));
                editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                editBtn.setTooltipText("Kunde bearbeiten");

                Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
                    try {
                        customerService.deleteCustomer(customer.getId());
                        updateList();
                        Notification.show("Kunde archiviert.");
                    } catch (IllegalStateException ex) {
                        Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeNames("error");
                    }
                });
                deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                deleteBtn.setTooltipText("Kunde archivieren");

                layout.add(editBtn, deleteBtn);
            } else {
                Button restoreBtn = new Button(VaadinIcon.REFRESH.create(), e -> {
                    customer.setDeleted(false);
                    customerService.saveCustomer(customer);
                    updateList();
                    Notification.show("Kunde reaktiviert.");
                });
                restoreBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
                restoreBtn.setTooltipText("Kunde reaktivieren");
                layout.add(restoreBtn);

                if (accessChecker.hasAccess(UserView.class)) {
                    Button hardDeleteBtn = new Button(VaadinIcon.ERASER.create(),
                            e -> openHardDeleteConfirmDialog(customer));
                    hardDeleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
                    hardDeleteBtn.setTooltipText("Kunde dauerhaft löschen");
                    layout.add(hardDeleteBtn);
                }
            }
            return layout;
        }).setHeader("Aktionen").setAutoWidth(true);

        grid.setSizeFull();
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter nach Name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        Button addButton = new Button("Neuer Kunde", VaadinIcon.PLUS.create(), e -> openCustomerDialog(new Customer()));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.setTooltipText("Neuen Kunden erstellen");

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addButton);

        if (accessChecker.hasAccess(UserView.class)) {
            showArchived.addValueChangeListener(e -> updateList());
            toolbar.add(showArchived);
        }
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void openCustomerDialog(Customer customer) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(customer.getId() == null ? "Kunde erstellen" : "Kunde bearbeiten");

        TextField name = new TextField("Kundenname");
        name.setValue(customer.getName() != null ? customer.getName() : "");
        name.setRequired(true);

        TextField email = new TextField("E-Mail");
        email.setValue(customer.getEmail() != null ? customer.getEmail() : "");

        TextField address = new TextField("Adresse");
        address.setValue(customer.getAddress() != null ? customer.getAddress() : "");
        address.setRequired(true);

        Button saveBtn = new Button("Speichern", e -> {
            if (name.getValue() == null || name.getValue().trim().isEmpty()) {
                Notification.show("Der Kundenname ist erforderlich.");
                name.setInvalid(true);
                return;
            }
            if (address.getValue() == null || address.getValue().trim().isEmpty()) {
                Notification.show("Die Adresse ist erforderlich.");
                address.setInvalid(true);
                return;
            }
            try {
                customer.setName(name.getValue());
                customer.setEmail(email.getValue());
                customer.setAddress(address.getValue());
                customerService.saveCustomer(customer);
                dialog.close();
                updateList();
                Notification.show("Kunde gespeichert.");
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(new VerticalLayout(name, email, address));
        dialog.getFooter().add(new Button("Abbrechen", e -> dialog.close()), saveBtn);
        dialog.open();
    }

    private void updateList() {
        boolean isAdmin = accessChecker.hasAccess(UserView.class);
        boolean showAll = isAdmin && showArchived.getValue();
        grid.setItems(customerService.findAllCustomers(filterText.getValue(), showAll));
    }

    private void openHardDeleteConfirmDialog(Customer customer) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Dauerhaft löschen?");

        VerticalLayout content = new VerticalLayout(
                new Html("<p>Möchten Sie den Kunden <b>" + customer.getName() + "</b> wirklich dauerhaft löschen?</p>"),
                new Span("Dieser Vorgang kann nicht rückgängig gemacht werden.")
        );
        content.setPadding(false);

        Button confirmBtn = new Button("Dauerhaft löschen", VaadinIcon.ERASER.create(), e -> {
            customerService.hardDeleteCustomer(customer.getId());
            updateList();
            confirmDialog.close();
            Notification.show("Kunde dauerhaft gelöscht.");
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        confirmBtn.setTooltipText("Kunde dauerhaft aus der Datenbank löschen");

        confirmDialog.add(content);
        confirmDialog.getFooter().add(new Button("Abbrechen", e -> confirmDialog.close()), confirmBtn);
        confirmDialog.open();
    }
}