package com.attus.financial.service;

import com.attus.financial.domain.entity.Account;
import com.attus.financial.domain.entity.Transaction;
import com.attus.financial.domain.enums.AccountStatus;
import com.attus.financial.dto.request.TransactionRequest;
import com.attus.financial.dto.response.TransactionResponse;
import com.attus.financial.exception.BusinessException;
import com.attus.financial.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final AuditLogService auditLogService;

    @Transactional
    public TransactionResponse execute(TransactionRequest request) {
        log.info("[TRANSACTION] Iniciando {} de R${} na conta {}", request.getType(), request.getAmount(), request.getAccountId());
        Account account = accountService.getAccountOrThrow(request.getAccountId());
        validateAccountActive(account);

        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter;

        switch (request.getType()) {
            case DEPOSIT -> {
                balanceAfter = balanceBefore.add(request.getAmount());
                account.setBalance(balanceAfter);
            }
            case WITHDRAWAL -> {
                validateSufficientBalance(account, request.getAmount());
                balanceAfter = balanceBefore.subtract(request.getAmount());
                account.setBalance(balanceAfter);
            }
            case TRANSFER -> {
                if (request.getDestinationAccountId() == null) {
                    throw new BusinessException("MISSING_DESTINATION", "Conta destino é obrigatória para transferência", HttpStatus.BAD_REQUEST);
                }
                validateSufficientBalance(account, request.getAmount());
                Account destination = accountService.getAccountOrThrow(request.getDestinationAccountId());
                validateAccountActive(destination);
                balanceAfter = balanceBefore.subtract(request.getAmount());
                account.setBalance(balanceAfter);
                destination.setBalance(destination.getBalance().add(request.getAmount()));
            }
            default -> throw new BusinessException("INVALID_TRANSACTION_TYPE", "Tipo de transação inválido", HttpStatus.BAD_REQUEST);
        }

        Transaction transaction = Transaction.builder()
                .account(account)
                .type(request.getType())
                .amount(request.getAmount())
                .description(request.getDescription())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .status("SUCCESS")
                .build();

        Transaction saved = transactionRepository.save(transaction);
        auditLogService.log("Transaction", saved.getId().toString(), request.getType().name(),
                "Valor: " + request.getAmount() + " | Saldo: " + balanceBefore + " -> " + balanceAfter);
        log.info("[TRANSACTION] {} concluída. Saldo: {} -> {}", request.getType(), balanceBefore, balanceAfter);
        return TransactionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> findByAccount(UUID accountId, Pageable pageable) {
        accountService.getAccountOrThrow(accountId);
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable)
                .map(TransactionResponse::from);
    }

    private void validateAccountActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("ACCOUNT_NOT_ACTIVE",
                    "Conta " + account.getNumber() + " não está ativa. Status: " + account.getStatus(),
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private void validateSufficientBalance(Account account, BigDecimal amount) {
        BigDecimal minBalance = account.getProduct().getMinBalance();
        BigDecimal afterWithdrawal = account.getBalance().subtract(amount);
        if (afterWithdrawal.compareTo(minBalance) < 0) {
            throw new BusinessException("INSUFFICIENT_BALANCE",
                    "Saldo insuficiente. Disponível: R$" + account.getBalance() + " | Saldo mínimo: R$" + minBalance,
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }
}
