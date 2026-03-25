package ch.ipa.jacqueline.frontend;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Dashboard")
@Route(value = "", layout = MainLayout.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_USER"})
public class DashboardView extends VerticalLayout {

    public DashboardView() {
        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        add(new H2("Willkommen im TaskManager"));
        add(new Span("Nutzen Sie das Menü auf der linken Seite, um die Verwaltungen aufzurufen."));
    }
}