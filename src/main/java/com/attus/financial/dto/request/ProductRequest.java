package com.attus.financial.dto.request;

import com.attus.financial.domain.enums.ProductType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String name;

    private String description;

    @NotNull(message = "Tipo do produto é obrigatório")
    private ProductType type;

    @DecimalMin(value = "0.0", message = "Taxa de juros não pode ser negativa")
    @Digits(integer = 3, fraction = 2, message = "Taxa de juros inválida")
    private BigDecimal interestRate;

    @DecimalMin(value = "0.0", message = "Saldo mínimo não pode ser negativo")
    private BigDecimal minBalance = BigDecimal.ZERO;
}
