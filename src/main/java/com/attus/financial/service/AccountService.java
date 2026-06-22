package com.attus.financial.service;

import com.attus.financial.domain.entity.Account;
import com.attus.financial.domain.entity.Customer;
import com.attus.financial.domain.entity.Product;
import com.attus.financial.domain.enums.AccountStatus;
import com.attus.financial.domain.enums.ProductStatus;
import com.attus.financial.dto.request.AccountRequest;
import com.attus.financial.dto.response.AccountResponse;
import com.attus.financial.exception.BusinessException;
import com.attus.financial.exception.ResourceNotFoundException;
import com.attus.financial.repository.AccountRepository;
import com.attus.financial.repository.CustomerRepository;
import com.attus.financial.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public AccountResponse open(AccountRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getCustomerId()));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto", request.getProductId()));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BusinessException("PRODUCT_INACTIVE", "Produto inativo não aceita novas contas", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Account account = Account.builder()
                .number(generateAccountNumber())
                .customer(customer)
                .product(product)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();
        Account saved = accountRepository.save(account);
        auditLogService.log("Account", saved.getId().toString(), "CREATE", "Conta " + saved.getNumber());
        log.info("[ACCOUNT] Conta aberta: {} para cliente: {}", saved.getNumber(), customer.getName());
        return AccountResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> findAll() {
        return accountRepository.findAll().stream().map(AccountResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountResponse findById(UUID id) {
        return AccountResponse.from(getAccountOrThrow(id));
    }

    @Transactional
    public AccountResponse changeStatus(UUID id, AccountStatus newStatus) {
        Account account = getAccountOrThrow(id);
        if (newStatus == AccountStatus.CLOSED && account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("BALANCE_NOT_ZERO", "Conta só pode ser encerrada com saldo zero", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        account.setStatus(newStatus);
        Account saved = accountRepository.save(account);
        auditLogService.log("Account", id.toString(), "STATUS_CHANGE", newStatus.name());
        return AccountResponse.from(saved);
    }

    public Account getAccountOrThrow(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta", id));
    }

    private String generateAccountNumber() {
        return String.format("%08d-%d",
                (int)(Math.random() * 99999999),
                (int)(Math.random() * 9));
    }
}
