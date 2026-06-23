package com.attus.financial.repository;

import com.attus.financial.domain.entity.Account;
import com.attus.financial.domain.entity.Customer;
import com.attus.financial.domain.entity.Product;
import com.attus.financial.domain.enums.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Account Repository")
class AccountRepositoryTest {

    @Autowired private AccountRepository accountRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;

    private Customer persistedCustomer;
    private Product persistedProduct;

    @BeforeEach
    void setUp() {
        persistedCustomer = customerRepository.save(Customer.builder()
                .name("Test User").cpf("529.982.247-25").email("test@repo.com")
                .birthDate(LocalDate.of(1990, 1, 1)).status(CustomerStatus.ACTIVE).build());

        persistedProduct = productRepository.save(Product.builder()
                .name("Conta Corrente Test").type(ProductType.CHECKING)
                .minBalance(BigDecimal.ZERO).status(ProductStatus.ACTIVE).build());
    }

    // ─── findByCustomerId ────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given a customer with two associated accounts")
    class GivenCustomerWithTwoAccounts {

        @Test
        @DisplayName("When findByCustomerId is called, then both accounts should be returned")
        void whenFindByCustomerIdCalled_thenBothAccountsShouldBeReturned() {
            accountRepository.save(Account.builder()
                    .number("00000001-1").customer(persistedCustomer).product(persistedProduct)
                    .balance(BigDecimal.ZERO).status(AccountStatus.ACTIVE).build());
            accountRepository.save(Account.builder()
                    .number("00000002-2").customer(persistedCustomer).product(persistedProduct)
                    .balance(new BigDecimal("500.00")).status(AccountStatus.ACTIVE).build());

            List<Account> accounts = accountRepository.findByCustomerId(persistedCustomer.getId());

            assertThat(accounts).hasSize(2);
            assertThat(accounts).extracting(Account::getNumber).containsExactlyInAnyOrder("00000001-1", "00000002-2");
        }
    }

    @Nested
    @DisplayName("Given a customer with no accounts")
    class GivenCustomerWithNoAccounts {

        @Test
        @DisplayName("When findByCustomerId is called, then an empty list should be returned")
        void whenFindByCustomerIdCalled_thenEmptyListShouldBeReturned() {
            Customer customerWithNoAccounts = customerRepository.save(Customer.builder()
                    .name("Sem Conta").cpf("111.222.333-96").email("noaccount@repo.com")
                    .birthDate(LocalDate.of(1985, 6, 15)).status(CustomerStatus.ACTIVE).build());

            List<Account> accounts = accountRepository.findByCustomerId(customerWithNoAccounts.getId());

            assertThat(accounts).isEmpty();
        }
    }

    // ─── findByStatus ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given accounts with mixed statuses")
    class GivenAccountsWithMixedStatuses {

        @Test
        @DisplayName("When findByStatus(ACTIVE) is called, then only active accounts should be returned")
        void whenFindByActiveStatusCalled_thenOnlyActiveAccountsShouldBeReturned() {
            accountRepository.save(Account.builder()
                    .number("10000001-1").customer(persistedCustomer).product(persistedProduct)
                    .balance(BigDecimal.ZERO).status(AccountStatus.ACTIVE).build());
            accountRepository.save(Account.builder()
                    .number("10000002-2").customer(persistedCustomer).product(persistedProduct)
                    .balance(BigDecimal.ZERO).status(AccountStatus.BLOCKED).build());
            accountRepository.save(Account.builder()
                    .number("10000003-3").customer(persistedCustomer).product(persistedProduct)
                    .balance(BigDecimal.ZERO).status(AccountStatus.CLOSED).build());

            List<Account> activeAccounts = accountRepository.findByStatus(AccountStatus.ACTIVE);

            assertThat(activeAccounts).allMatch(a -> a.getStatus() == AccountStatus.ACTIVE);
        }
    }

    // ─── existsByCustomerIdAndProductId ──────────────────────────────────────

    @Nested
    @DisplayName("Given an account that already links a customer to a product")
    class GivenExistingAccountLinkingCustomerToProduct {

        @Test
        @DisplayName("When existsByCustomerIdAndProductId is called for that pair, then true should be returned")
        void whenExistsByCustomerIdAndProductIdCalled_thenTrueShouldBeReturned() {
            accountRepository.save(Account.builder()
                    .number("20000001-1").customer(persistedCustomer).product(persistedProduct)
                    .balance(BigDecimal.ZERO).status(AccountStatus.ACTIVE).build());

            boolean exists = accountRepository.existsByCustomerIdAndProductId(
                    persistedCustomer.getId(), persistedProduct.getId());

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("When existsByCustomerIdAndProductId is called for a different pair, then false should be returned")
        void whenExistsByCustomerIdAndProductIdCalledForDifferentPair_thenFalseShouldBeReturned() {
            boolean exists = accountRepository.existsByCustomerIdAndProductId(
                    persistedCustomer.getId(), persistedProduct.getId());

            assertThat(exists).isFalse();
        }
    }

    // ─── hasAccountsLinkedToProduct ───────────────────────────────────────────

    @Nested
    @DisplayName("Given a product with at least one linked account")
    class GivenProductWithLinkedAccount {

        @Test
        @DisplayName("When hasAccountsLinkedToProduct is called, then true should be returned")
        void whenHasAccountsLinkedToProductCalled_thenTrueShouldBeReturned() {
            accountRepository.save(Account.builder()
                    .number("30000001-1").customer(persistedCustomer).product(persistedProduct)
                    .balance(BigDecimal.ZERO).status(AccountStatus.ACTIVE).build());

            boolean hasLinks = accountRepository.hasAccountsLinkedToProduct(persistedProduct.getId());

            assertThat(hasLinks).isTrue();
        }
    }

    @Nested
    @DisplayName("Given a product with no linked accounts")
    class GivenProductWithNoLinkedAccounts {

        @Test
        @DisplayName("When hasAccountsLinkedToProduct is called, then false should be returned")
        void whenHasAccountsLinkedToProductCalled_thenFalseShouldBeReturned() {
            Product orphanProduct = productRepository.save(Product.builder()
                    .name("Produto Sem Conta").type(ProductType.SAVINGS)
                    .minBalance(BigDecimal.ZERO).status(ProductStatus.ACTIVE).build());

            boolean hasLinks = accountRepository.hasAccountsLinkedToProduct(orphanProduct.getId());

            assertThat(hasLinks).isFalse();
        }
    }
}
