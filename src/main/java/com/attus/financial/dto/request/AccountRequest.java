package com.attus.financial.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class AccountRequest {
    @NotNull(message = "Cliente é obrigatório")
    private UUID customerId;

    @NotNull(message = "Produto é obrigatório")
    private UUID productId;
}
