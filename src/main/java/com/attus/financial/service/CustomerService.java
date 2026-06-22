package com.attus.financial.service;

import com.attus.financial.domain.entity.Customer;
import com.attus.financial.domain.enums.CustomerStatus;
import com.attus.financial.dto.request.CustomerRequest;
import com.attus.financial.dto.response.AccountResponse;
import com.attus.financial.dto.response.CustomerResponse;
import com.attus.financial.exception.BusinessException;
import com.attus.financial.exception.ResourceNotFoundException;
import com.attus.financial.repository.AccountRepository;
import com.attus.financial.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        log.info("[CUSTOMER] Criando cliente CPF: {}", request.getCpf());
        if (customerRepository.existsByCpf(request.getCpf())) {
            throw new BusinessException("CPF_ALREADY_EXISTS", "CPF já cadastrado", HttpStatus.CONFLICT);
        }
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("EMAIL_ALREADY_EXISTS", "E-mail já cadastrado", HttpStatus.CONFLICT);
        }
        Customer customer = Customer.builder()
                .name(request.getName()).cpf(request.getCpf())
                .email(request.getEmail()).phone(request.getPhone())
                .birthDate(request.getBirthDate()).status(CustomerStatus.ACTIVE)
                .build();
        Customer saved = customerRepository.save(customer);
        auditLogService.log("Customer", saved.getId().toString(), "CREATE", saved.getName());
        return CustomerResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream()
                .map(CustomerResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(UUID id) {
        return CustomerResponse.from(getCustomerOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> findAccountsByCustomerId(UUID id) {
        getCustomerOrThrow(id);
        return accountRepository.findByCustomerId(id).stream()
                .map(AccountResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = getCustomerOrThrow(id);
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setBirthDate(request.getBirthDate());
        Customer saved = customerRepository.save(customer);
        auditLogService.log("Customer", id.toString(), "UPDATE", saved.getName());
        return CustomerResponse.from(saved);
    }

    @Transactional
    public void toggleStatus(UUID id) {
        Customer customer = getCustomerOrThrow(id);
        CustomerStatus newStatus = customer.getStatus() == CustomerStatus.ACTIVE
                ? CustomerStatus.INACTIVE : CustomerStatus.ACTIVE;
        customer.setStatus(newStatus);
        customerRepository.save(customer);
        auditLogService.log("Customer", id.toString(), "STATUS_CHANGE", newStatus.name());
    }

    private Customer getCustomerOrThrow(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }
}
