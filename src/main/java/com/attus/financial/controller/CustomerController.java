package com.attus.financial.controller;

import com.attus.financial.dto.request.CustomerRequest;
import com.attus.financial.dto.response.AccountResponse;
import com.attus.financial.dto.response.ApiResponse;
import com.attus.financial.dto.response.CustomerResponse;
import com.attus.financial.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gerenciamento de clientes")
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Cadastrar cliente")
    public ResponseEntity<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(customerService.create(request)));
    }

    @GetMapping
    @Operation(summary = "Listar todos os clientes")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(customerService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    public ResponseEntity<ApiResponse<CustomerResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.findById(id)));
    }

    @GetMapping("/{id}/accounts")
    @Operation(summary = "Listar contas do cliente")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> findAccounts(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.findAccountsByCustomerId(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados do cliente")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(@PathVariable UUID id,
                                                                @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(customerService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Alternar status do cliente")
    public ResponseEntity<Void> toggleStatus(@PathVariable UUID id) {
        customerService.toggleStatus(id);
        return ResponseEntity.noContent().build();
    }
}
