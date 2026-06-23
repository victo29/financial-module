package com.attus.financial.service;

import com.attus.financial.domain.entity.Account;
import com.attus.financial.domain.entity.Customer;
import com.attus.financial.domain.entity.Product;
import com.attus.financial.domain.enums.AccountStatus;
import com.attus.financial.domain.enums.CustomerStatus;
import com.attus.financial.domain.enums.ProductStatus;
import com.attus.financial.domain.enums.ProductType;
import com.attus.financial.dto.request.AccountRequest;
import com.attus.financial.dto.response.AccountResponse;
import com.attus.financial.exception.BusinessException;
import com.attus.financial.exception.ResourceNotFoundException;
import com.attus.financial.repository.AccountRepository;
import com.attus.financial.repository.CustomerRepository;
import com.attus.financial.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Account Service")
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ProductRepository productRepository;
    @Mock private AuditLogService auditLogService;
    @InjectMocks private AccountService accountService;

    private Customer buildActiveCustomer() {
        return Customer.builder()
                .id(UUID.randomUUID()).name("João Silva").cpf("123.456.789-09")
                .email("joao@email.com").birthDate(LocalDate.of(1990, 1, 1))
                .status(CustomerStatus.ACTIVE).build();
    }

    private Product buildActiveProduct() {
        return Product.builder()
                .id(UUID.randomUUID()).name("Conta Corrente")
                .type(ProductType.CHECKING).minBalance(BigDecimal.ZERO)
                .status(ProductStatus.ACTIVE).build();
    }

    private Account buildAccount(Customer customer, Product product, BigDecimal balance, AccountStatus status) {
        return Account.builder()
                .id(UUID.randomUUID()).number("12345678-9")
                .customer(customer).product(product)
                .balance(balance).status(status).build();
    }

    // ─── OPEN ACCOUNT ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given a valid active customer and an active product")
    class GivenValidActiveCustomerAndActiveProduct {

        @Test
        @DisplayName("When an account is opened, then it should be persisted with ACTIVE status and zero balance")
        void whenAccountIsOpened_thenItShouldBeActiveWithZeroBalance() {
            Customer customer = buildActiveCustomer();
            Product product = buildActiveProduct();
            AccountRequest request = new AccountRequest();
            request.setCustomerId(customer.getId());
            request.setProductId(product.getId());

            Account saved = buildAccount(customer, product, BigDecimal.ZERO, AccountStatus.ACTIVE);
            when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
            when(accountRepository.save(any(Account.class))).thenReturn(saved);

            AccountResponse response = accountService.open(request);

            assertThat(response.getStatus()).isEqualTo(AccountStatus.ACTIVE);
            assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.getCustomerName()).isEqualTo("João Silva");
            verify(auditLogService).log(eq("Account"), any(), eq("CREATE"), any());
        }
    }

    @Nested
    @DisplayName("Given a valid active customer and an INACTIVE product")
    class GivenValidCustomerAndInactiveProduct {

        @Test
        @DisplayName("When account opening is attempted, then BusinessException with code PRODUCT_INACTIVE should be thrown")
        void whenAccountOpeningAttempted_thenProductInactiveExceptionShouldBeThrown() {
            Customer customer = buildActiveCustomer();
            Product inactiveProduct = buildActiveProduct();
            inactiveProduct.setStatus(ProductStatus.INACTIVE);
            AccountRequest request = new AccountRequest();
            request.setCustomerId(customer.getId());
            request.setProductId(inactiveProduct.getId());

            when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
            when(productRepository.findById(inactiveProduct.getId())).thenReturn(Optional.of(inactiveProduct));

            assertThatThrownBy(() -> accountService.open(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Produto inativo");

            verify(accountRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Given an account request for a non-existent customer")
    class GivenNonExistentCustomer {

        @Test
        @DisplayName("When account opening is attempted, then ResourceNotFoundException should be thrown")
        void whenAccountOpeningAttempted_thenResourceNotFoundExceptionShouldBeThrown() {
            UUID nonExistentCustomerId = UUID.randomUUID();
            AccountRequest request = new AccountRequest();
            request.setCustomerId(nonExistentCustomerId);
            request.setProductId(UUID.randomUUID());

            when(customerRepository.findById(nonExistentCustomerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.open(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cliente");
        }
    }

    // ─── FIND ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given multiple accounts in the repository")
    class GivenMultipleAccountsInRepository {

        @Test
        @DisplayName("When findAll is called, then all accounts should be returned")
        void whenFindAllCalled_thenAllAccountsShouldBeReturned() {
            Customer customer = buildActiveCustomer();
            Product product = buildActiveProduct();
            when(accountRepository.findAll()).thenReturn(List.of(
                    buildAccount(customer, product, new BigDecimal("100.00"), AccountStatus.ACTIVE),
                    buildAccount(customer, product, new BigDecimal("200.00"), AccountStatus.ACTIVE)
            ));

            List<AccountResponse> result = accountService.findAll();

            assertThat(result).hasSize(2);
        }
    }

    // ─── CHANGE STATUS ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given an active account with a non-zero balance")
    class GivenActiveAccountWithNonZeroBalance {

        @Test
        @DisplayName("When closure is attempted, then BusinessException with code BALANCE_NOT_ZERO should be thrown")
        void whenClosureAttempted_thenBalanceNotZeroExceptionShouldBeThrown() {
            UUID id = UUID.randomUUID();
            Account account = buildAccount(buildActiveCustomer(), buildActiveProduct(),
                    new BigDecimal("500.00"), AccountStatus.ACTIVE);
            when(accountRepository.findById(id)).thenReturn(Optional.of(account));

            assertThatThrownBy(() -> accountService.changeStatus(id, AccountStatus.CLOSED))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("saldo zero");

            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("When block is requested, then account status should become BLOCKED without restriction on balance")
        void whenBlockIsRequested_thenAccountStatusShouldBecomeBlocked() {
            UUID id = UUID.randomUUID();
            Account account = buildAccount(buildActiveCustomer(), buildActiveProduct(),
                    new BigDecimal("1000.00"), AccountStatus.ACTIVE);
            when(accountRepository.findById(id)).thenReturn(Optional.of(account));
            when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

            AccountResponse response = accountService.changeStatus(id, AccountStatus.BLOCKED);

            assertThat(response.getStatus()).isEqualTo(AccountStatus.BLOCKED);
        }
    }

    @Nested
    @DisplayName("Given an active account with zero balance")
    class GivenActiveAccountWithZeroBalance {

        @Test
        @DisplayName("When closure is requested, then account status should become CLOSED and audit log recorded")
        void whenClosureRequested_thenAccountStatusShouldBecomeClosedAndAuditLogged() {
            UUID id = UUID.randomUUID();
            Account account = buildAccount(buildActiveCustomer(), buildActiveProduct(),
                    BigDecimal.ZERO, AccountStatus.ACTIVE);
            when(accountRepository.findById(id)).thenReturn(Optional.of(account));
            when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

            AccountResponse response = accountService.changeStatus(id, AccountStatus.CLOSED);

            assertThat(response.getStatus()).isEqualTo(AccountStatus.CLOSED);
            verify(auditLogService).log(eq("Account"), any(), eq("STATUS_CHANGE"), eq("CLOSED"));
        }
    }

    @Nested
    @DisplayName("Given a non-existent account ID")
    class GivenNonExistentAccountId {

        @Test
        @DisplayName("When getAccountOrThrow is called, then ResourceNotFoundException should be thrown")
        void whenGetAccountOrThrowCalled_thenResourceNotFoundExceptionShouldBeThrown() {
            UUID nonExistentId = UUID.randomUUID();
            when(accountRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.getAccountOrThrow(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Conta");
        }
    }
}
