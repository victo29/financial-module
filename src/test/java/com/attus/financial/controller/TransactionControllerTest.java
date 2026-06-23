package com.attus.financial.controller;

import com.attus.financial.domain.enums.ProductType;
import com.attus.financial.domain.enums.TransactionType;
import com.attus.financial.dto.request.AccountRequest;
import com.attus.financial.dto.request.CustomerRequest;
import com.attus.financial.dto.request.ProductRequest;
import com.attus.financial.dto.request.TransactionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Transaction Controller")
class TransactionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String accountId;
    private String secondAccountId;

    @BeforeEach
    void setUp() throws Exception {
        String customerId = createCustomer("529.982.247-25", "txn-test@test.com");
        String productId = createProduct("Conta Corrente BDD", ProductType.CHECKING);
        accountId = openAccount(customerId, productId);
        secondAccountId = openAccount(createCustomer("987.654.321-00", "second@test.com"), productId);
    }

    private String createCustomer(String cpf, String email) throws Exception {
        CustomerRequest req = new CustomerRequest();
        req.setName("Test User");
        req.setCpf(cpf);
        req.setEmail(email);
        req.setBirthDate(LocalDate.of(1990, 1, 1));
        MvcResult result = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).at("/data/id").asText();
    }

    private String createProduct(String name, ProductType type) throws Exception {
        ProductRequest req = new ProductRequest();
        req.setName(name);
        req.setType(type);
        req.setMinBalance(BigDecimal.ZERO);
        MvcResult result = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).at("/data/id").asText();
    }

    private String openAccount(String customerId, String productId) throws Exception {
        AccountRequest req = new AccountRequest();
        req.setCustomerId(UUID.fromString(customerId));
        req.setProductId(UUID.fromString(productId));
        MvcResult result = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).at("/data/id").asText();
    }

    private void deposit(String accId, BigDecimal amount) throws Exception {
        TransactionRequest req = new TransactionRequest();
        req.setAccountId(UUID.fromString(accId));
        req.setType(TransactionType.DEPOSIT);
        req.setAmount(amount);
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));
    }

    // ─── DEPOSIT ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given an active account with R$0.00 balance")
    class GivenActiveAccountWithZeroBalance {

        @Test
        @DisplayName("When a DEPOSIT of R$500.00 is executed, then a 201 response with balanceAfter=500.00 should be returned")
        void whenDepositOf500Executed_then201WithBalanceAfter500() throws Exception {
            TransactionRequest request = new TransactionRequest();
            request.setAccountId(UUID.fromString(accountId));
            request.setType(TransactionType.DEPOSIT);
            request.setAmount(new BigDecimal("500.00"));
            request.setDescription("Depósito inicial");

            mockMvc.perform(post("/api/v1/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.type").value("DEPOSIT"))
                    .andExpect(jsonPath("$.data.balanceBefore").value(0.0))
                    .andExpect(jsonPath("$.data.balanceAfter").value(500.0))
                    .andExpect(jsonPath("$.data.status").value("SUCCESS"));
        }
    }

    // ─── WITHDRAWAL ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given an active account with R$1000.00 balance")
    class GivenActiveAccountWith1000Balance {

        @BeforeEach
        void depositFunds() throws Exception {
            deposit(accountId, new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("When a WITHDRAWAL of R$300.00 is executed, then a 201 response with balanceAfter=700.00 should be returned")
        void whenWithdrawalOf300Executed_then201WithBalanceAfter700() throws Exception {
            TransactionRequest request = new TransactionRequest();
            request.setAccountId(UUID.fromString(accountId));
            request.setType(TransactionType.WITHDRAWAL);
            request.setAmount(new BigDecimal("300.00"));

            mockMvc.perform(post("/api/v1/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.balanceBefore").value(1000.0))
                    .andExpect(jsonPath("$.data.balanceAfter").value(700.0));
        }

        @Test
        @DisplayName("When a WITHDRAWAL amount exceeds the balance, then a 422 response with INSUFFICIENT_BALANCE should be returned")
        void whenWithdrawalExceedsBalance_then422WithInsufficientBalance() throws Exception {
            TransactionRequest request = new TransactionRequest();
            request.setAccountId(UUID.fromString(accountId));
            request.setType(TransactionType.WITHDRAWAL);
            request.setAmount(new BigDecimal("5000.00"));

            mockMvc.perform(post("/api/v1/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value("INSUFFICIENT_BALANCE"));
        }
    }

    // ─── TRANSFER ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given two active accounts — origin with R$800.00 and destination with R$200.00")
    class GivenTwoAccountsForTransfer {

        @BeforeEach
        void depositFunds() throws Exception {
            deposit(accountId, new BigDecimal("800.00"));
            deposit(secondAccountId, new BigDecimal("200.00"));
        }

        @Test
        @DisplayName("When a TRANSFER of R$300.00 is executed, then origin balance becomes R$500.00 and destination R$500.00")
        void whenTransferOf300Executed_thenOriginAndDestinationBalancesUpdated() throws Exception {
            TransactionRequest request = new TransactionRequest();
            request.setAccountId(UUID.fromString(accountId));
            request.setType(TransactionType.TRANSFER);
            request.setAmount(new BigDecimal("300.00"));
            request.setDestinationAccountId(UUID.fromString(secondAccountId));

            mockMvc.perform(post("/api/v1/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.balanceAfter").value(500.0));

            mockMvc.perform(get("/api/v1/accounts/{id}", secondAccountId))
                    .andExpect(jsonPath("$.data.balance").value(500.0));
        }

        @Test
        @DisplayName("When a TRANSFER with no destination account is attempted, then a 400 response with MISSING_DESTINATION should be returned")
        void whenTransferWithNoDestination_then400WithMissingDestination() throws Exception {
            TransactionRequest request = new TransactionRequest();
            request.setAccountId(UUID.fromString(accountId));
            request.setType(TransactionType.TRANSFER);
            request.setAmount(new BigDecimal("100.00"));

            mockMvc.perform(post("/api/v1/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("MISSING_DESTINATION"));
        }
    }

    // ─── NON-ACTIVE ACCOUNT ──────────────────────────────────────────────────

    @Nested
    @DisplayName("Given a BLOCKED account")
    class GivenBlockedAccount {

        @Test
        @DisplayName("When any transaction is attempted, then a 422 response with ACCOUNT_NOT_ACTIVE should be returned")
        void whenTransactionOnBlockedAccount_then422WithAccountNotActive() throws Exception {
            mockMvc.perform(patch("/api/v1/accounts/{id}/status", accountId)
                    .param("status", "BLOCKED"));

            TransactionRequest request = new TransactionRequest();
            request.setAccountId(UUID.fromString(accountId));
            request.setType(TransactionType.DEPOSIT);
            request.setAmount(new BigDecimal("100.00"));

            mockMvc.perform(post("/api/v1/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_ACTIVE"));
        }
    }

    // ─── VALIDATION ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given a transaction request with an amount of zero")
    class GivenTransactionWithZeroAmount {

        @Test
        @DisplayName("When the request is processed, then a 400 response with VALIDATION_ERROR should be returned")
        void whenRequestWithZeroAmount_then400WithValidationError() throws Exception {
            TransactionRequest request = new TransactionRequest();
            request.setAccountId(UUID.fromString(accountId));
            request.setType(TransactionType.DEPOSIT);
            request.setAmount(BigDecimal.ZERO);

            mockMvc.perform(post("/api/v1/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
    }

    // ─── STATEMENT ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given an account with multiple transactions")
    class GivenAccountWithMultipleTransactions {

        @Test
        @DisplayName("When GET /accounts/{id}/transactions is called, then transactions should be ordered by date descending")
        void whenGetStatementCalled_thenTransactionsShouldBeOrderedByDateDescending() throws Exception {
            deposit(accountId, new BigDecimal("100.00"));
            deposit(accountId, new BigDecimal("200.00"));

            mockMvc.perform(get("/api/v1/accounts/{id}/transactions", accountId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }
    }
}
