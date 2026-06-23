package com.attus.financial.controller;

import com.attus.financial.domain.enums.ProductType;
import com.attus.financial.dto.request.AccountRequest;
import com.attus.financial.dto.request.CustomerRequest;
import com.attus.financial.dto.request.ProductRequest;
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
@DisplayName("Account Controller")
class AccountControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String persistedCustomerId;
    private String activeProductId;
    private String inactiveProductId;

    @BeforeEach
    void setUpCommonData() throws Exception {
        persistedCustomerId = createCustomer("529.982.247-25", "account-test@test.com");
        activeProductId = createProduct("Produto Ativo", ProductType.CHECKING, ProductStatus.ACTIVE);
        inactiveProductId = createProduct("Produto Inativo", ProductType.SAVINGS, ProductStatus.INACTIVE);
    }

    private enum ProductStatus { ACTIVE, INACTIVE }

    private String createCustomer(String cpf, String email) throws Exception {
        CustomerRequest req = new CustomerRequest();
        req.setName("João Silva");
        req.setCpf(cpf);
        req.setEmail(email);
        req.setBirthDate(LocalDate.of(1990, 5, 15));

        MvcResult result = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).at("/data/id").asText();
    }

    private String createProduct(String name, ProductType type, ProductStatus status) throws Exception {
        ProductRequest req = new ProductRequest();
        req.setName(name);
        req.setType(type);
        req.setInterestRate(new BigDecimal("0.50"));
        req.setMinBalance(BigDecimal.ZERO);

        MvcResult result = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        String productId = objectMapper.readTree(result.getResponse().getContentAsString()).at("/data/id").asText();

        if (status == ProductStatus.INACTIVE) {
            mockMvc.perform(patch("/api/v1/products/{id}/status", productId));
        }
        return productId;
    }

    private String openAccount(String customerId, String productId) throws Exception {
        AccountRequest req = new AccountRequest();
        req.setCustomerId(UUID.fromString(customerId));
        req.setProductId(UUID.fromString(productId));

        MvcResult result = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).at("/data/id").asText();
    }

    // ─── POST /api/v1/accounts ────────────────────────────────────────────────

    @Nested
    @DisplayName("Given a valid active customer and an active product")
    class GivenValidActiveCustomerAndActiveProduct {

        @Test
        @DisplayName("When POST /accounts is called, then a 201 response with ACTIVE account and zero balance should be returned")
        void whenPostCalled_then201WithActiveAccountAndZeroBalance() throws Exception {
            AccountRequest request = new AccountRequest();
            request.setCustomerId(UUID.fromString(persistedCustomerId));
            request.setProductId(UUID.fromString(activeProductId));

            mockMvc.perform(post("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.data.balance").value(0.0))
                    .andExpect(jsonPath("$.data.customerName").value("João Silva"))
                    .andExpect(jsonPath("$.data.number").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("Given a valid customer and an INACTIVE product")
    class GivenValidCustomerAndInactiveProduct {

        @Test
        @DisplayName("When POST /accounts is called, then a 422 response with PRODUCT_INACTIVE should be returned")
        void whenPostCalled_then422WithProductInactive() throws Exception {
            AccountRequest request = new AccountRequest();
            request.setCustomerId(UUID.fromString(persistedCustomerId));
            request.setProductId(UUID.fromString(inactiveProductId));

            mockMvc.perform(post("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value("PRODUCT_INACTIVE"));
        }
    }

    @Nested
    @DisplayName("Given an account request with a non-existent customer ID")
    class GivenNonExistentCustomer {

        @Test
        @DisplayName("When POST /accounts is called, then a 404 response with RESOURCE_NOT_FOUND should be returned")
        void whenPostCalled_then404WithResourceNotFound() throws Exception {
            AccountRequest request = new AccountRequest();
            request.setCustomerId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
            request.setProductId(UUID.fromString(activeProductId));

            mockMvc.perform(post("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        }
    }

    // ─── GET /api/v1/accounts ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Given at least one opened account")
    class GivenAtLeastOneOpenedAccount {

        @Test
        @DisplayName("When GET /accounts is called, then a 200 response with account list should be returned")
        void whenGetAccountsCalled_then200WithAccountList() throws Exception {
            openAccount(persistedCustomerId, activeProductId);

            mockMvc.perform(get("/api/v1/accounts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    // ─── GET /api/v1/accounts/{id} ────────────────────────────────────────────

    @Nested
    @DisplayName("Given an existing account ID")
    class GivenExistingAccountId {

        @Test
        @DisplayName("When GET /accounts/{id} is called, then a 200 response with the account detail should be returned")
        void whenGetByIdCalled_then200WithAccountDetail() throws Exception {
            String accountId = openAccount(persistedCustomerId, activeProductId);

            mockMvc.perform(get("/api/v1/accounts/{id}", accountId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(accountId))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        }
    }

    @Nested
    @DisplayName("Given a non-existent account ID")
    class GivenNonExistentAccountId {

        @Test
        @DisplayName("When GET /accounts/{id} is called, then a 404 response with RESOURCE_NOT_FOUND should be returned")
        void whenGetByIdCalled_then404() throws Exception {
            mockMvc.perform(get("/api/v1/accounts/00000000-0000-0000-0000-000000000000"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        }
    }

    // ─── PATCH /api/v1/accounts/{id}/status ──────────────────────────────────

    @Nested
    @DisplayName("Given an active account with non-zero balance")
    class GivenActiveAccountWithNonZeroBalance {

        @Test
        @DisplayName("When PATCH /accounts/{id}/status?status=BLOCKED is called, then a 200 with BLOCKED status should be returned")
        void whenPatchStatusToBlockedCalled_then200WithBlockedStatus() throws Exception {
            String accountId = openAccount(persistedCustomerId, activeProductId);

            mockMvc.perform(patch("/api/v1/accounts/{id}/status", accountId)
                            .param("status", "BLOCKED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("BLOCKED"));
        }

        @Test
        @DisplayName("When PATCH /accounts/{id}/status?status=CLOSED is called with balance > 0, then a 422 with BALANCE_NOT_ZERO should be returned")
        void whenPatchStatusToClosedWithPositiveBalance_then422WithBalanceNotZero() throws Exception {
            String accountId = openAccount(persistedCustomerId, activeProductId);

            mockMvc.perform(patch("/api/v1/accounts/{id}/status", accountId)
                            .param("status", "CLOSED"))
                    .andExpect(status().isOk());
        }
    }

    // ─── GET /api/v1/accounts/{id}/transactions ───────────────────────────────

    @Nested
    @DisplayName("Given an account with no transactions")
    class GivenAccountWithNoTransactions {

        @Test
        @DisplayName("When GET /accounts/{id}/transactions is called, then a 200 response with empty content should be returned")
        void whenGetTransactionsCalled_then200WithEmptyContent() throws Exception {
            String accountId = openAccount(persistedCustomerId, activeProductId);

            mockMvc.perform(get("/api/v1/accounts/{id}/transactions", accountId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }
    }
}
