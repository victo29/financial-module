package com.attus.financial.dto.response;

import com.attus.financial.domain.entity.Transaction;
import com.attus.financial.domain.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TransactionResponse {
    private UUID id;
    private UUID accountId;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String status;
    private LocalDateTime createdAt;

    public static TransactionResponse from(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .accountId(t.getAccount().getId())
                .type(t.getType()).amount(t.getAmount())
                .description(t.getDescription())
                .balanceBefore(t.getBalanceBefore())
                .balanceAfter(t.getBalanceAfter())
                .status(t.getStatus())
                .createdAt(t.getCreatedAt()).build();
    }
}
