package com.attus.financial.controller;

import com.attus.financial.domain.enums.ProductType;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Product Controller")
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String createProduct(String name, ProductType type) throws Exception {
        ProductRequest req = new ProductRequest();
        req.setName(name);
        req.setType(type);
        req.setInterestRate(new BigDecimal("0.50"));
        req.setMinBalance(BigDecimal.ZERO);

        MvcResult result = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/id").asText();
    }

    // ─── POST /api/v1/products ────────────────────────────────────────────────

    @Nested
    @DisplayName("Given a valid product creation request")
    class GivenValidProductCreationRequest {

        @Test
        @DisplayName("When the request is processed, then a 201 response with the product data should be returned")
        void whenRequestProcessed_then201WithProductData() throws Exception {
            ProductRequest request = new ProductRequest();
            request.setName("Poupança Premium");
            request.setType(ProductType.SAVINGS);
            request.setInterestRate(new BigDecimal("0.50"));
            request.setMinBalance(BigDecimal.ZERO);

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.name").value("Poupança Premium"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.data.id").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("Given a product creation request with a blank name")
    class GivenProductCreationRequestWithBlankName {

        @Test
        @DisplayName("When the request is processed, then a 400 response with VALIDATION_ERROR should be returned")
        void whenRequestProcessed_then400WithValidationError() throws Exception {
            ProductRequest request = new ProductRequest();
            request.setType(ProductType.SAVINGS);

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.data.name").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("Given a product creation request with a name shorter than 3 characters")
    class GivenProductCreationRequestWithShortName {

        @Test
        @DisplayName("When the request is processed, then a 400 response with VALIDATION_ERROR should be returned")
        void whenRequestProcessed_then400WithValidationError() throws Exception {
            ProductRequest request = new ProductRequest();
            request.setName("AB");
            request.setType(ProductType.SAVINGS);
            request.setMinBalance(BigDecimal.ZERO);

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
    }

    // ─── GET /api/v1/products ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Given an existing set of products")
    class GivenExistingProducts {

        @Test
        @DisplayName("When GET /products is called, then a 200 response with a product list should be returned")
        void whenGetProductsCalled_then200WithProductList() throws Exception {
            createProduct("Produto BDD", ProductType.CHECKING);

            mockMvc.perform(get("/api/v1/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    // ─── GET /api/v1/products/{id} ────────────────────────────────────────────

    @Nested
    @DisplayName("Given an existing product ID")
    class GivenExistingProductId {

        @Test
        @DisplayName("When GET /products/{id} is called, then a 200 response with the product should be returned")
        void whenGetByIdCalled_then200WithProduct() throws Exception {
            String id = createProduct("Produto Pesquisa", ProductType.INVESTMENT);

            mockMvc.perform(get("/api/v1/products/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(id))
                    .andExpect(jsonPath("$.data.name").value("Produto Pesquisa"));
        }
    }

    @Nested
    @DisplayName("Given a non-existent product ID")
    class GivenNonExistentProductId {

        @Test
        @DisplayName("When GET /products/{id} is called, then a 404 response with RESOURCE_NOT_FOUND should be returned")
        void whenGetByIdCalled_then404WithResourceNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/products/00000000-0000-0000-0000-000000000000"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        }
    }

    // ─── PUT /api/v1/products/{id} ────────────────────────────────────────────

    @Nested
    @DisplayName("Given an existing product and a valid update request")
    class GivenExistingProductAndValidUpdateRequest {

        @Test
        @DisplayName("When PUT /products/{id} is called, then a 200 response with the updated data should be returned")
        void whenPutCalled_then200WithUpdatedData() throws Exception {
            String id = createProduct("Produto Original", ProductType.SAVINGS);

            ProductRequest updateRequest = new ProductRequest();
            updateRequest.setName("Produto Atualizado");
            updateRequest.setType(ProductType.SAVINGS);
            updateRequest.setInterestRate(new BigDecimal("1.00"));
            updateRequest.setMinBalance(new BigDecimal("50.00"));

            mockMvc.perform(put("/api/v1/products/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("Produto Atualizado"))
                    .andExpect(jsonPath("$.data.minBalance").value(50.00));
        }
    }

    // ─── PATCH /api/v1/products/{id}/status ──────────────────────────────────

    @Nested
    @DisplayName("Given an ACTIVE product")
    class GivenActiveProduct {

        @Test
        @DisplayName("When PATCH /products/{id}/status is called, then a 204 response should be returned and status toggled")
        void whenPatchStatusCalled_then204AndStatusToggled() throws Exception {
            String id = createProduct("Produto Toggle", ProductType.CREDIT);

            mockMvc.perform(patch("/api/v1/products/{id}/status", id))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/v1/products/{id}", id))
                    .andExpect(jsonPath("$.data.status").value("INACTIVE"));
        }
    }

    // ─── DELETE /api/v1/products/{id} ─────────────────────────────────────────

    @Nested
    @DisplayName("Given a product with no linked accounts")
    class GivenProductWithNoLinkedAccounts {

        @Test
        @DisplayName("When DELETE /products/{id} is called, then a 204 response should be returned")
        void whenDeleteCalled_then204() throws Exception {
            String id = createProduct("Produto Excluivel", ProductType.CHECKING);

            mockMvc.perform(delete("/api/v1/products/{id}", id))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/v1/products/{id}", id))
                    .andExpect(status().isNotFound());
        }
    }
}
