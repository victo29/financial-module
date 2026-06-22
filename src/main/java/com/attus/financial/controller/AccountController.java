package com.attus.financial.controller;

import com.attus.financial.domain.enums.AccountStatus;
import com.attus.financial.dto.request.AccountRequest;
import com.attus.financial.dto.response.AccountResponse;
import com.attus.financial.dto.response.ApiResponse;
import com.attus.financial.dto.response.TransactionResponse;
import com.attus.financial.service.AccountService;
import com.attus.financial.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Contas", description = "Gerenciamento de contas financeiras")
@CrossOrigin(origins = "*")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Abrir conta (associar cliente a produto)")
    public ResponseEntity<ApiResponse<AccountResponse>> open(@Valid @RequestBody AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(accountService.open(request)));
    }

    @GetMapping
    @Operation(summary = "Listar todas as contas")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(accountService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe da conta")
    public ResponseEntity<ApiResponse<AccountResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.findById(id)));
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "Extrato da conta (paginado)")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getStatement(@PathVariable UUID id, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(transactionService.findByAccount(id, pageable)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Alterar status da conta")
    public ResponseEntity<ApiResponse<AccountResponse>> changeStatus(@PathVariable UUID id,
                                                                      @RequestParam AccountStatus status) {
        return ResponseEntity.ok(ApiResponse.success(accountService.changeStatus(id, status)));
    }
}
