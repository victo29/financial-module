package com.attus.financial.service;

import com.attus.financial.domain.entity.Account;
import com.attus.financial.domain.entity.Customer;
import com.attus.financial.domain.entity.Product;
import com.attus.financial.domain.entity.Transaction;
import com.attus.financial.domain.enums.AccountStatus;
import com.attus.financial.domain.enums.CustomerStatus;
import com.attus.financial.domain.enums.ProductStatus;
import com.attus.financial.domain.enums.ProductType;
import com.attus.financial.domain.enums.TransactionType;
import com.attus.financial.dto.request.TransactionRequest;
import com.attus.financial.dto.response.TransactionResponse;
import com.attus.financial.exception.BusinessException;
import com.attus.financial.exception.ResourceNotFoundException;
import com.attus.financial.repository.TransactionRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transaction Service")
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountService accountService;
    @Mock private AuditLogService auditLogService;
    @InjectMocks private TransactionService transactionService;

    private Account buildAccount(BigDecimal balance, BigDecimal minBalance, AccountStatus status) {
        Customer customer = Customer.builder()
                .id(UUID.randomUUID()).name("Teste").cpf("111.222.333-96")
                .email("teste@email.com").birthDate(LocalDate.of(1990, 1, 1))
                .status(CustomerStatus.ACTIVE).build();
        Product product = Product.builder()
                .id(UUID.randomUUID()).name("Produto").type(ProductType.CHECKING)
                .minBalance(minBalance).status(ProductStatus.ACTIVE).build();
        return Account.builder()
                .id(UUID.randomUUID()).number("12345678-9")
                .customer(customer).product(product)
                .balance(balance).status(status).build();
    }

    private void setupSaveMock() {
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            return Transaction.builder()
                    .id(UUID.randomUUID()).account(t.getAccount())
                    .type(t.getType()).amount(t.getAmount())
                    .balanceBefore(t.getBalanceBefore()).balanceAfter(t.getBalanceAfter())
                    .status("SUCCESS").build();
        });
    }

    // ─── DEPOSIT ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given an active account with R$1000.00 balance")
    class GivenActiveAccountWith1000Balance {

        private Account account;

        @BeforeEach
        void setUp() {
            account = buildAccount(new BigDecimal("1000.00"), BigDecimal.ZERO, AccountStatus.ACTIVE);
            setupSaveMock();
        }

        @Test
        @DisplayName("When a DEPOSIT of R$500.00 is executed, then balance should increase to R$1500.00")
        void whenDepositOf500Executed_thenBalanceShouldIncreaseTo1500() {
            TransactionRequest request = new TransactionRequest();
            request.setAccountId(account.getId());
            request.setType(TransactionType.DEPOSIT);
            request.setAmount(new BigDecimal("500.00"));

            when(accountService.getAccountOrThrow(account.getId())).thenReturn(account);

            TransactionResponse response = transactionService.execute(request);

            assertThat(response.getBalanceBefore()).isEqualByComparingTo("1000.00");
            assertThat(response.getBalanceAfter()).isEqualByComparingTo("1500.00");
            assertThat(account.getBalance()).isEqualByComparingTo("1500.00");
            assertThat(response.getStatus()).isEqualTo("SUCCESS");
        }

        @Test
        @DisplayName("When a WITHDRAWAL of R$300.00 is executed, then balance should decrease to R$700.00")
        void whenWithdrawalOf300Executed_thenBalanceShouldDecreaseTo700() {
            TransactionRequest request = new TransactionRequest();
            request.setAccountId(account.getId());
            request.setType(TransactionType.WITHDRAWAL);
            request.setAmount(new BigDecimal("300.00"));

            when(accountService.getAccountOrThrow(account.getId())).thenReturn(account);

            TransactionResponse response = transactionService.execute(request);

            assertThat(response.getBalanceAfter()).isEqualByComparingTo("700.00");
            assertThat(account.getBalance()).isEqualByComparingTo("700.00");
        }

        @Test
        @DisplayName("When a DEPOSIT is executed, then an audit log entry should be recorded")
        void whenDepositExecuted_thenAuditLogEntryShouldBeRecorded() {
            TransactionRequest request = new TransactionRequest();
            request.setAccountId(account.getId());
            request.setType(TransactionType.DEPOSIT);
            request.setAmount(new BigDecimal("100.00"));

            when(accountService.getAccountOrThrow(account.getId())).thenReturn(account);

            transactionService.execute(request);

            verify(auditLogService).log(eq("Transaction"), any(), eq("DEPOSIT"), any());
        }
    }

    // ─── INSUFFICIENT BALANCE ────────────────────────────────────────────────

    @Nested
    @DisplayName("Given an active account with R$100.00 balance and no minimum balance")
    class GivenAccountWithInsufficientFunds {

        @Test
        @DisplayName("When a WITHDRAWAL of R$500.00 is attempted, then BusinessException INSUFFICIENT_BALANCE should be thrown")
        void whenWithdrawalExceedsBalance_thenInsufficientBalanceExceptionShouldBeThrown() {
            Account account = buildAccount(new BigDecimal("100.00"), BigDecimal.ZERO, AccountStatus.ACTIVE);
            TransactionRequest request = new TransactionRequest();
            request.setAccountId(account.getId());
            request.setType(TransactionType.WITHDRAWAL);
            request.setAmount(new BigDecimal("500.00"));

            when(accountService.getAccountOrThrow(account.getId())).thenReturn(account);

            assertThatThrownBy(() -> transactionService.execute(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Saldo insuficiente");

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("When a TRANSFER of R$500.00 is attempted, then BusinessException INSUFFICIENT_BALANCE should be thrown")
        void whenTransferExceedsBalance_thenInsufficientBalanceExceptionShouldBeThrown() {
            Account origin = buildAccount(new BigDecimal("100.00"), BigDecimal.ZERO, AccountStatus.ACTIVE);
            Account destination = buildAccount(BigDecimal.ZERO, BigDecimal.ZERO, AccountStatus.ACTIVE);
            TransactionRequest request = new TransactionRequest();
            request.setAccountId(origin.getId());
            request.setType(TransactionType.TRANSFER);
            request.setAmount(new BigDecimal("500.00"));
            request.setDestinationAccountId(destination.getId());

            when(accountService.getAccountOrThrow(origin.getId())).thenReturn(origin);

            assertThatThrownBy(() -> transactionService.execute(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Saldo insuficiente");
        }
    }

    // ─── MINIMUM BALANCE CONSTRAINT ─────────────────────────────────────────

    @Nested
    @DisplayName("Given an account with R$150.00 balance and R$100.00 minimum balance")
    class GivenAccountWithMinimumBalance {

        @Test
        @DisplayName("When a WITHDRAWAL of R$100.00 would violate the minimum balance, then INSUFFICIENT_BALANCE should be thrown")
        void whenWithdrawalViolatesMinimumBalance_thenInsufficientBalanceExceptionShouldBeThrown() {
            Account account = buildAccount(new BigDecimal("150.00"), new BigDecimal("100.00"), AccountStatus.ACTIVE);
            TransactionRequest request = new TransactionRequest();
            request.setAccountId(account.getId());
            request.setType(TransactionType.WITHDRAWAL);
            request.setAmount(new BigDecimal("100.00"));

            when(accountService.getAccountOrThrow(account.getId())).thenReturn(account);

            assertThatThrownBy(() -> transactionService.execute(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Saldo insuficiente");
        }

        @Test
        @DisplayName("When a WITHDRAWAL of R$50.00 respects the minimum balance, then it should succeed")
        void whenWithdrawalRespectsMinimumBalance_thenItShouldSucceed() {
            Account account = buildAccount(new BigDecimal("150.00"), new BigDecimal("100.00"), AccountStatus.ACTIVE);
            TransactionRequest request = new TransactionRequest();
            request.setAccountId(account.getId());
            request.setType(TransactionType.WITHDRAWAL);
            request.setAmount(new BigDecimal("50.00"));

            when(accountService.getAccountOrThrow(account.getId())).thenReturn(account);
            setupSaveMock();

            TransactionResponse response = transactionService.execute(request);

            assertThat(response.getBalanceAfter()).isEqualByComparingTo("100.00");
        }
    }

    // ─── TRANSFER ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given two active accounts with sufficient balance for a transfer")
    class GivenTwoActiveAccountsForTransfer {

        @Test
        @DisplayName("When a TRANSFER of R$400.00 is executed, then origin decreases to R$600.00 and destination increases to R$600.00")
        void whenTransferExecuted_thenOriginDecreasesAndDestinationIncreases() {
            Account origin = buildAccount(new BigDecimal("1000.00"), BigDecimal.ZERO, AccountStatus.ACTIVE);
            Account destination = buildAccount(new BigDecimal("200.00"), BigDecimal.ZERO, AccountStatus.ACTIVE);
            TransactionRequest request = new TransactionRequest();
            request.setAccountId(origin.getId());
            request.setType(TransactionType.TRANSFER);
            request.setAmount(new BigDecimal("400.00"));
            request.setDestinationAccountId(destination.getId());

            when(accountService.getAccountOrThrow(origin.getId())).thenReturn(origin);
            when(accountService.getAccountOrThrow(destination.getId())).thenReturn(destination);
            setupSaveMock();

            transactionService.execute(request);

            assertThat(origin.getBalance()).isEqualByComparingTo("600.00");
            assertThat(destination.getBalance()).isEqualByComparingTo("600.00");
        }
    }

    @Nested
    @DisplayName("Given a TRANSFER request with no destination account specified")
    class GivenTransferWithoutDestinationAccount {

        @Test
        @DisplayName("When the transfer is executed, then BusinessException MISSING_DESTINATION should be thrown")
        void whenTransferExecuted_thenMissingDestinationExceptionShouldBeThrown() {
            Account account = buildAccount(new BigDecimal("1000.00"), BigDecimal.ZERO, AccountStatus.ACTIVE);
            TransactionRequest request = new TransactionRequest();
            request.setAccountId(account.getId());
            request.setType(TransactionType.TRANSFER);
            request.setAmount(new BigDecimal("100.00"));

            when(accountService.getAccountOrThrow(account.getId())).thenReturn(account);

            assertThatThrownBy(() -> transactionService.execute(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Conta destino é obrigatória");
        }
    }

    // ─── NON-ACTIVE ACCOUNTS ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Given a BLOCKED account")
    class GivenBlockedAccount {

        @Test
        @DisplayName("When any transaction is attempted, then BusinessException ACCOUNT_NOT_ACTIVE should be thrown")
        void whenAnyTransactionAttempted_thenAccountNotActiveExceptionShouldBeThrown() {
            Account blockedAccount = buildAccount(new BigDecimal("500.00"), BigDecimal.ZERO, AccountStatus.BLOCKED);
            TransactionRequest request = new TransactionRequest();
            request.setAccountId(blockedAccount.getId());
            request.setType(TransactionType.DEPOSIT);
            request.setAmount(new BigDecimal("100.00"));

            when(accountService.getAccountOrThrow(blockedAccount.getId())).thenReturn(blockedAccount);

            assertThatThrownBy(() -> transactionService.execute(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("não está ativa");
        }
    }

    @Nested
    @DisplayName("Given a CLOSED account")
    class GivenClosedAccount {

        @Test
        @DisplayName("When any transaction is attempted, then BusinessException ACCOUNT_NOT_ACTIVE should be thrown")
        void whenAnyTransactionAttempted_thenAccountNotActiveExceptionShouldBeThrown() {
            Account closedAccount = buildAccount(BigDecimal.ZERO, BigDecimal.ZERO, AccountStatus.CLOSED);
            TransactionRequest request = new TransactionRequest();
            request.setAccountId(closedAccount.getId());
            request.setType(TransactionType.DEPOSIT);
            request.setAmount(new BigDecimal("100.00"));

            when(accountService.getAccountOrThrow(closedAccount.getId())).thenReturn(closedAccount);

            assertThatThrownBy(() -> transactionService.execute(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("não está ativa");
        }
    }

    // ─── TRANSFER TO BLOCKED DESTINATION ─────────────────────────────────────

    @Nested
    @DisplayName("Given an active origin account and a BLOCKED destination account")
    class GivenActiveOriginAndBlockedDestination {

        @Test
        @DisplayName("When a TRANSFER is attempted, then BusinessException ACCOUNT_NOT_ACTIVE should be thrown for the destination")
        void whenTransferAttempted_thenAccountNotActiveExceptionShouldBeThrownForDestination() {
            Account origin = buildAccount(new BigDecimal("1000.00"), BigDecimal.ZERO, AccountStatus.ACTIVE);
            Account blockedDestination = buildAccount(BigDecimal.ZERO, BigDecimal.ZERO, AccountStatus.BLOCKED);
            TransactionRequest request = new TransactionRequest();
            request.setAccountId(origin.getId());
            request.setType(TransactionType.TRANSFER);
            request.setAmount(new BigDecimal("200.00"));
            request.setDestinationAccountId(blockedDestination.getId());

            when(accountService.getAccountOrThrow(origin.getId())).thenReturn(origin);
            when(accountService.getAccountOrThrow(blockedDestination.getId())).thenReturn(blockedDestination);

            assertThatThrownBy(() -> transactionService.execute(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("não está ativa");
        }
    }

    // ─── NON-EXISTENT ACCOUNT ────────────────────────────────────────────────

    @Nested
    @DisplayName("Given a transaction request for a non-existent account")
    class GivenNonExistentAccountId {

        @Test
        @DisplayName("When the transaction is attempted, then ResourceNotFoundException should be thrown")
        void whenTransactionAttempted_thenResourceNotFoundExceptionShouldBeThrown() {
            UUID nonExistentId = UUID.randomUUID();
            TransactionRequest request = new TransactionRequest();
            request.setAccountId(nonExistentId);
            request.setType(TransactionType.WITHDRAWAL);
            request.setAmount(new BigDecimal("100.00"));

            when(accountService.getAccountOrThrow(nonExistentId))
                    .thenThrow(new ResourceNotFoundException("Conta", nonExistentId));

            assertThatThrownBy(() -> transactionService.execute(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Conta");
        }
    }
}
