package ch.ipa.jacqueline.services;

import ch.ipa.jacqueline.backend.entities.Customer;
import ch.ipa.jacqueline.backend.repositories.CustomerRepository;
import ch.ipa.jacqueline.backend.repositories.ProjectRepository;
import ch.ipa.jacqueline.backend.services.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    @DisplayName("UT-01: Kunde: Create - Anlage eines neuen Kunden mit validen Stammdaten")
    void createCustomer_WithValidData_ReturnsCustomer() {
        Customer customer = new Customer(null, "Pritz IT GmbH", "info@pritz-it.com", "Richtistrasse 7", false);
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer result = customerService.createCustomer(customer);

        assertThat(result.getName()).isEqualTo("Pritz IT GmbH");
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("UT-02: Kunde: Read - Abruf eines existierenden Kunden über seine ID")
    void findById_WithExistingActiveCustomer_ReturnsCustomer() {
        Customer customer = new Customer(1L, "Pritz IT GmbH", "info@pritz-it.com", "Richtistrasse 7", false);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Optional<Customer> result = customerService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Pritz IT GmbH");
    }

    @Test
    @DisplayName("UT-03: Kunde: Update - Änderung des Kundennamens bei einem bestehenden Datensatz")
    void updateCustomer_ChangesName_UpdatedCorrectly() {
        Customer existing = new Customer(1L, "Alt GmbH", "alt@test.com", "Alte Strasse 1", false);
        Customer updated = new Customer(1L, "Neu GmbH", "neu@test.com", "Neue Strasse 2", false);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(existing)).thenReturn(existing);

        Customer result = customerService.updateCustomer(updated);

        assertThat(result.getName()).isEqualTo("Neu GmbH");
        verify(customerRepository).save(existing);
    }

    @Test
    @DisplayName("UT-04: Kunde: Delete - Durchführung eines Soft-Deletes für einen Kunden")
    void deleteCustomer_SetsDeletedFlag_CustomerMarkedAsDeleted() {
        Customer customer = new Customer(1L, "Pritz IT GmbH", "info@pritz-it.com", "Richtistrasse 7", false);
        when(projectRepository.countByCustomerId(1L)).thenReturn(0L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerService.deleteCustomer(1L);

        assertThat(customer.isDeleted()).isTrue();
        verify(customerRepository).save(customer);
    }
}