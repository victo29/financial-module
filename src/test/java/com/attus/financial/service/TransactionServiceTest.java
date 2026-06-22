package com.attus.financial.service;

import com.attus.financial.domain.enums.TransactionType;
import com.attus.financial.dto.request.TransactionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransactionServiceTest {

    @Autowired private TransactionService transactionService;
    @Autowired private AccountService accountService;

    @Test
    void shouldThrowExceptionWhenInsufficientBalance() {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.WITHDRAWAL);
        request.setAmount(new BigDecimal("1000.00"));
        request.setAccountId(UUID.randomUUID());

        assertThrows(Exception.class, () -> transactionService.execute(request));
    }
}
