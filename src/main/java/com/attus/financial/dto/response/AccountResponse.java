package com.attus.financial.dto.response;

import com.attus.financial.domain.entity.Account;
import com.attus.financial.domain.enums.AccountStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AccountResponse {
    private UUID id;
    private String number;
    private UUID customerId;
    private String customerName;
    private UUID productId;
    private String productName;
    private BigDecimal balance;
    private AccountStatus status;
    private LocalDateTime openedAt;

    public static AccountResponse from(Account a) {
        return AccountResponse.builder()
                .id(a.getId()).number(a.getNumber())
                .customerId(a.getCustomer().getId())
                .customerName(a.getCustomer().getName())
                .productId(a.getProduct().getId())
                .productName(a.getProduct().getName())
                .balance(a.getBalance()).status(a.getStatus())
                .openedAt(a.getOpenedAt()).build();
    }
}
