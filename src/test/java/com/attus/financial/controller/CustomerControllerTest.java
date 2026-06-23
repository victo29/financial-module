package com.attus.financial.controller;

import com.attus.financial.dto.request.CustomerRequest;
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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Customer Controller")
class CustomerControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private CustomerRequest buildValidRequest(String cpf, String email) {
        CustomerRequest req = new CustomerRequest();
        req.setName("João Silva");
        req.setCpf(cpf);
        req.setEmail(email);
        req.setPhone("(11) 99999-1111");
        req.setBirthDate(LocalDate.of(1990, 5, 15));
        return req;
    }

    private String createCustomer(String cpf, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidRequest(cpf, email))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).at("/data/id").asText();
    }

    // ─── POST /api/v1/customers ───────────────────────────────────────────────

    @Nested
    @DisplayName("Given a valid customer registration request with unique CPF and email")
    class GivenValidCustomerRegistrationRequest {

        @Test
        @DisplayName("When the request is processed, then a 201 response with ACTIVE customer data should be returned")
        void whenRequestProcessed_then201WithActiveCustomerData() throws Exception {
            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildValidRequest("529.982.247-25", "joao@test.com"))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.name").value("João Silva"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.data.id").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("Given a customer registration request with an already-registered CPF")
    class GivenDuplicateCpf {

        @Test
        @DisplayName("When the request is processed, then a 409 response with CPF_ALREADY_EXISTS should be returned")
        void whenRequestProcessed_then409WithCpfAlreadyExists() throws Exception {
            createCustomer("529.982.247-25", "first@test.com");

            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildValidRequest("529.982.247-25", "second@test.com"))))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("CPF_ALREADY_EXISTS"));
        }
    }

    @Nested
    @DisplayName("Given a customer registration request with an already-registered email")
    class GivenDuplicateEmail {

        @Test
        @DisplayName("When the request is processed, then a 409 response with EMAIL_ALREADY_EXISTS should be returned")
        void whenRequestProcessed_then409WithEmailAlreadyExists() throws Exception {
            createCustomer("529.982.247-25", "shared@test.com");

            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildValidRequest("987.654.321-00", "shared@test.com"))))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
        }
    }

    @Nested
    @DisplayName("Given a customer registration request with a blank name")
    class GivenBlankName {

        @Test
        @DisplayName("When the request is processed, then a 400 response with VALIDATION_ERROR should be returned")
        void whenRequestProcessed_then400WithValidationError() throws Exception {
            CustomerRequest request = buildValidRequest("529.982.247-25", "blank@test.com");
            request.setName("");

            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
    }

    @Nested
    @DisplayName("Given a customer registration request with an invalid CPF format")
    class GivenInvalidCpfFormat {

        @Test
        @DisplayName("When the request is processed, then a 400 response with VALIDATION_ERROR should be returned")
        void whenRequestProcessed_then400WithValidationError() throws Exception {
            CustomerRequest request = buildValidRequest("12345678900", "valid@test.com");

            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
    }

    @Nested
    @DisplayName("Given a customer registration request with a future birth date")
    class GivenFutureBirthDate {

        @Test
        @DisplayName("When the request is processed, then a 400 response with VALIDATION_ERROR should be returned")
        void whenRequestProcessed_then400WithValidationError() throws Exception {
            CustomerRequest request = buildValidRequest("529.982.247-25", "future@test.com");
            request.setBirthDate(LocalDate.now().plusDays(1));

            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
    }

    // ─── GET /api/v1/customers ────────────────────────────────────────────────

    @Nested
    @DisplayName("Given at least one registered customer")
    class GivenAtLeastOneRegisteredCustomer {

        @Test
        @DisplayName("When GET /customers is called, then a 200 response with a customer list should be returned")
        void whenGetCustomersCalled_then200WithCustomerList() throws Exception {
            createCustomer("529.982.247-25", "list@test.com");

            mockMvc.perform(get("/api/v1/customers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    // ─── GET /api/v1/customers/{id} ───────────────────────────────────────────

    @Nested
    @DisplayName("Given an existing customer ID")
    class GivenExistingCustomerId {

        @Test
        @DisplayName("When GET /customers/{id} is called, then a 200 response with the customer data should be returned")
        void whenGetByIdCalled_then200WithCustomerData() throws Exception {
            String id = createCustomer("529.982.247-25", "findme@test.com");

            mockMvc.perform(get("/api/v1/customers/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(id))
                    .andExpect(jsonPath("$.data.cpf").value("529.982.247-25"));
        }
    }

    @Nested
    @DisplayName("Given a non-existent customer ID")
    class GivenNonExistentCustomerId {

        @Test
        @DisplayName("When GET /customers/{id} is called, then a 404 response with RESOURCE_NOT_FOUND should be returned")
        void whenGetByIdCalled_then404WithResourceNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/customers/00000000-0000-0000-0000-000000000000"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        }
    }

    // ─── PUT /api/v1/customers/{id} ───────────────────────────────────────────

    @Nested
    @DisplayName("Given an existing customer and a valid update request")
    class GivenExistingCustomerAndUpdateRequest {

        @Test
        @DisplayName("When PUT /customers/{id} is called, then a 200 response with updated customer data should be returned")
        void whenPutCalled_then200WithUpdatedCustomerData() throws Exception {
            String id = createCustomer("529.982.247-25", "update@test.com");

            CustomerRequest updateRequest = buildValidRequest("529.982.247-25", "update@test.com");
            updateRequest.setName("João Atualizado");
            updateRequest.setPhone("(21) 88888-9999");

            mockMvc.perform(put("/api/v1/customers/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("João Atualizado"))
                    .andExpect(jsonPath("$.data.phone").value("(21) 88888-9999"));
        }
    }

    // ─── PATCH /api/v1/customers/{id}/status ─────────────────────────────────

    @Nested
    @DisplayName("Given an ACTIVE customer")
    class GivenActiveCustomer {

        @Test
        @DisplayName("When PATCH /customers/{id}/status is called, then a 204 response should be returned and status toggled to INACTIVE")
        void whenPatchStatusCalled_then204AndStatusToggledToInactive() throws Exception {
            String id = createCustomer("529.982.247-25", "toggle@test.com");

            mockMvc.perform(patch("/api/v1/customers/{id}/status", id))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/v1/customers/{id}", id))
                    .andExpect(jsonPath("$.data.status").value("INACTIVE"));
        }
    }
}
