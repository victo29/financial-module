package com.attus.financial.repository;

import com.attus.financial.domain.entity.Account;
import com.attus.financial.domain.entity.Customer;
import com.attus.financial.domain.entity.Product;
import com.attus.financial.domain.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AccountRepositoryTest {

    @Autowired private AccountRepository accountRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;

    @Test
    void shouldFindAccountsByCustomerId() {
        Customer customer = customerRepository.save(Customer.builder()
                .name("Teste").cpf("111.222.333-96").email("teste@email.com")
                .birthDate(LocalDate.of(1990, 1, 1)).status(CustomerStatus.ACTIVE).build());

        Product product = productRepository.save(Product.builder()
                .name("Produto Teste").type(ProductType.CHECKING)
                .minBalance(BigDecimal.ZERO).status(ProductStatus.ACTIVE).build());

        accountRepository.save(Account.builder()
                .number("12345678-9").customer(customer).product(product)
                .balance(BigDecimal.ZERO).status(AccountStatus.ACTIVE).build());

        List<Account> accounts = accountRepository.findByCustomerId(customer.getId());
        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getCustomer().getName()).isEqualTo("Teste");
    }
}
