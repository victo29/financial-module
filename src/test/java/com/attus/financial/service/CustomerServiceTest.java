package com.attus.financial.service;

import com.attus.financial.domain.entity.Customer;
import com.attus.financial.domain.enums.CustomerStatus;
import com.attus.financial.dto.request.CustomerRequest;
import com.attus.financial.dto.response.CustomerResponse;
import com.attus.financial.exception.BusinessException;
import com.attus.financial.exception.ResourceNotFoundException;
import com.attus.financial.repository.AccountRepository;
import com.attus.financial.repository.CustomerRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Service")
class CustomerServiceTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private AuditLogService auditLogService;
    @InjectMocks private CustomerService customerService;

    private CustomerRequest buildValidRequest() {
        CustomerRequest req = new CustomerRequest();
        req.setName("João Silva");
        req.setCpf("123.456.789-09");
        req.setEmail("joao.silva@email.com");
        req.setPhone("(11) 99999-1111");
        req.setBirthDate(LocalDate.of(1990, 5, 15));
        return req;
    }

    private Customer buildActiveCustomer(UUID id) {
        return Customer.builder()
                .id(id).name("João Silva").cpf("123.456.789-09")
                .email("joao.silva@email.com").phone("(11) 99999-1111")
                .birthDate(LocalDate.of(1990, 5, 15)).status(CustomerStatus.ACTIVE).build();
    }

    // ─── CREATE ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given a new customer with unique CPF and email")
    class GivenNewCustomerWithUniqueData {

        @Test
        @DisplayName("When the customer is created, then it should be persisted with ACTIVE status")
        void whenCustomerIsCreated_thenItShouldBePersistedWithActiveStatus() {
            UUID id = UUID.randomUUID();
            CustomerRequest request = buildValidRequest();
            when(customerRepository.existsByCpf(request.getCpf())).thenReturn(false);
            when(customerRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(customerRepository.save(any(Customer.class))).thenReturn(buildActiveCustomer(id));

            CustomerResponse response = customerService.create(request);

            assertThat(response.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
            assertThat(response.getName()).isEqualTo("João Silva");
            assertThat(response.getCpf()).isEqualTo("123.456.789-09");
        }

        @Test
        @DisplayName("When the customer is created, then an audit log CREATE entry should be recorded")
        void whenCustomerIsCreated_thenAuditLogCreateEntryShouldBeRecorded() {
            UUID id = UUID.randomUUID();
            CustomerRequest request = buildValidRequest();
            when(customerRepository.existsByCpf(any())).thenReturn(false);
            when(customerRepository.existsByEmail(any())).thenReturn(false);
            when(customerRepository.save(any(Customer.class))).thenReturn(buildActiveCustomer(id));

            customerService.create(request);

            verify(auditLogService).log(eq("Customer"), any(), eq("CREATE"), any());
        }
    }

    @Nested
    @DisplayName("Given a customer registration attempt with an already-registered CPF")
    class GivenDuplicateCpf {

        @Test
        @DisplayName("When customer creation is attempted, then BusinessException with code CPF_ALREADY_EXISTS should be thrown")
        void whenCustomerCreationAttempted_thenCpfAlreadyExistsExceptionShouldBeThrown() {
            CustomerRequest request = buildValidRequest();
            when(customerRepository.existsByCpf(request.getCpf())).thenReturn(true);

            assertThatThrownBy(() -> customerService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("CPF já cadastrado");

            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Given a customer registration attempt with an already-registered email")
    class GivenDuplicateEmail {

        @Test
        @DisplayName("When customer creation is attempted, then BusinessException with code EMAIL_ALREADY_EXISTS should be thrown")
        void whenCustomerCreationAttempted_thenEmailAlreadyExistsExceptionShouldBeThrown() {
            CustomerRequest request = buildValidRequest();
            when(customerRepository.existsByCpf(request.getCpf())).thenReturn(false);
            when(customerRepository.existsByEmail(request.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> customerService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("E-mail já cadastrado");

            verify(customerRepository, never()).save(any());
        }
    }

    // ─── FIND ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given multiple customers in the repository")
    class GivenMultipleCustomersInRepository {

        @Test
        @DisplayName("When findAll is called, then all customers should be returned")
        void whenFindAllCalled_thenAllCustomersShouldBeReturned() {
            Customer c1 = buildActiveCustomer(UUID.randomUUID());
            Customer c2 = buildActiveCustomer(UUID.randomUUID());
            c2.setName("Maria Santos");
            when(customerRepository.findAll()).thenReturn(List.of(c1, c2));

            List<CustomerResponse> result = customerService.findAll();

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Given a non-existent customer ID")
    class GivenNonExistentCustomerId {

        @Test
        @DisplayName("When findById is called, then ResourceNotFoundException should be thrown")
        void whenFindByIdCalled_thenResourceNotFoundExceptionShouldBeThrown() {
            UUID nonExistentId = UUID.randomUUID();
            when(customerRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.findById(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cliente");
        }
    }

    // ─── UPDATE ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given an existing customer and an update with new contact data")
    class GivenExistingCustomerAndUpdatePayload {

        @Test
        @DisplayName("When the customer is updated, then name, email and phone should reflect the new values")
        void whenCustomerIsUpdated_thenContactDataShouldReflectNewValues() {
            UUID id = UUID.randomUUID();
            Customer existing = buildActiveCustomer(id);
            CustomerRequest updateReq = buildValidRequest();
            updateReq.setName("João Santos");
            updateReq.setEmail("joao.santos@email.com");
            updateReq.setPhone("(11) 88888-2222");

            when(customerRepository.findById(id)).thenReturn(Optional.of(existing));
            when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

            CustomerResponse response = customerService.update(id, updateReq);

            assertThat(response.getName()).isEqualTo("João Santos");
            assertThat(response.getEmail()).isEqualTo("joao.santos@email.com");
            verify(auditLogService).log(eq("Customer"), any(), eq("UPDATE"), any());
        }
    }

    // ─── TOGGLE STATUS ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given an ACTIVE customer")
    class GivenActiveCustomer {

        @Test
        @DisplayName("When toggleStatus is called, then status should transition to INACTIVE")
        void whenToggleStatusCalled_thenStatusShouldTransitionToInactive() {
            UUID id = UUID.randomUUID();
            Customer customer = buildActiveCustomer(id);
            when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
            when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

            customerService.toggleStatus(id);

            assertThat(customer.getStatus()).isEqualTo(CustomerStatus.INACTIVE);
            verify(auditLogService).log(eq("Customer"), any(), eq("STATUS_CHANGE"), eq("INACTIVE"));
        }
    }

    @Nested
    @DisplayName("Given an INACTIVE customer")
    class GivenInactiveCustomer {

        @Test
        @DisplayName("When toggleStatus is called, then status should transition to ACTIVE")
        void whenToggleStatusCalled_thenStatusShouldTransitionToActive() {
            UUID id = UUID.randomUUID();
            Customer customer = buildActiveCustomer(id);
            customer.setStatus(CustomerStatus.INACTIVE);
            when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
            when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

            customerService.toggleStatus(id);

            assertThat(customer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
            verify(auditLogService).log(eq("Customer"), any(), eq("STATUS_CHANGE"), eq("ACTIVE"));
        }
    }
}
