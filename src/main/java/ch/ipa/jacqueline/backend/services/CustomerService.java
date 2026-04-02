package ch.ipa.jacqueline.backend.services;

import ch.ipa.jacqueline.backend.entities.Customer;
import ch.ipa.jacqueline.backend.repositories.CustomerRepository;
import ch.ipa.jacqueline.backend.repositories.ProjectRepository;
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
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ProjectRepository projectRepository;

    public List<Customer> findAllActiveCustomers(String filterText) {
        String filter = (filterText == null) ? "" : filterText;
        return customerRepository.findAllWithFilterNative(filter, false);
    }

    public Optional<Customer> findById(long id) {
        return customerRepository.findById(id)
                .filter(customer -> !customer.isDeleted());
    }

    public List<Customer> findAllCustomers(String filterText, boolean showArchived) {
        String filter = (filterText == null) ? "" : filterText;
        return customerRepository.findAllWithFilterNative(filter, showArchived);
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        log.info("Erstelle neuen Kunden: {}", customer.getName());
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer updateCustomer(Customer customer) {
        Customer existingCustomer = customerRepository.findByIdIgnoreDeleted(customer.getId())
                .orElseThrow(() -> new IllegalArgumentException("Kunde nicht gefunden"));
        existingCustomer.setName(customer.getName());
        existingCustomer.setEmail(customer.getEmail());
        existingCustomer.setAddress(customer.getAddress());
        existingCustomer.setDeleted(customer.isDeleted());
        log.info("Aktualisiere Kunden: {}", customer.getName());
        return customerRepository.save(existingCustomer);
    }

    @Transactional
    public void saveCustomer(Customer customer) {
        if (customer.getId() != null) {
            updateCustomer(customer);
        } else {
            createCustomer(customer);
        }
    }

    @Transactional
    public void deleteCustomer(long id) {
        if (projectRepository.countByCustomerId(id) > 0) {
            throw new IllegalStateException("Kunde kann nicht gelöscht werden, da ihm noch Projekte zugeordnet sind.");
        }
        customerRepository.findById(id).ifPresent(customer -> {
            customer.setDeleted(true);
            customerRepository.save(customer);
            log.info("Kunde archiviert: {}", customer.getName());
        });
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void hardDeleteCustomer(long id) {
        customerRepository.deleteById(id);
        log.info("Kunde dauerhaft gelöscht: ID {}", id);
    }
}