package com.attus.financial.dto.response;

import com.attus.financial.domain.entity.Product;
import com.attus.financial.domain.enums.ProductStatus;
import com.attus.financial.domain.enums.ProductType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ProductResponse {
    private UUID id;
    private String name;
    private String description;
    private ProductType type;
    private BigDecimal interestRate;
    private BigDecimal minBalance;
    private ProductStatus status;
    private LocalDateTime createdAt;

    public static ProductResponse from(Product p) {
        return ProductResponse.builder()
                .id(p.getId()).name(p.getName()).description(p.getDescription())
                .type(p.getType()).interestRate(p.getInterestRate())
                .minBalance(p.getMinBalance()).status(p.getStatus())
                .createdAt(p.getCreatedAt()).build();
    }
}
