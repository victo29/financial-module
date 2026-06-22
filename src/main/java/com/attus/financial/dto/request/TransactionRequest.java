package com.attus.financial.dto.request;

import com.attus.financial.domain.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransactionRequest {

    @NotNull(message = "Conta de origem é obrigatória")
    private UUID accountId;

    @NotNull(message = "Tipo da transação é obrigatório")
    private TransactionType type;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal amount;

    private String description;

    private UUID destinationAccountId;
}
