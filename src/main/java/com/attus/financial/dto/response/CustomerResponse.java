package com.attus.financial.dto.response;

import com.attus.financial.domain.entity.Customer;
import com.attus.financial.domain.enums.CustomerStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CustomerResponse {
    private UUID id;
    private String name;
    private String cpf;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private CustomerStatus status;
    private LocalDateTime createdAt;

    public static CustomerResponse from(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId()).name(c.getName()).cpf(c.getCpf())
                .email(c.getEmail()).phone(c.getPhone())
                .birthDate(c.getBirthDate()).status(c.getStatus())
                .createdAt(c.getCreatedAt()).build();
    }
}
