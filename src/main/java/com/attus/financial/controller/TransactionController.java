package com.attus.financial.controller;

import com.attus.financial.dto.request.TransactionRequest;
import com.attus.financial.dto.response.ApiResponse;
import com.attus.financial.dto.response.TransactionResponse;
import com.attus.financial.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transações", description = "Depósitos, saques e transferências")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Executar transação (DEPOSIT, WITHDRAWAL ou TRANSFER)")
    public ResponseEntity<ApiResponse<TransactionResponse>> execute(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(transactionService.execute(request)));
    }
}
