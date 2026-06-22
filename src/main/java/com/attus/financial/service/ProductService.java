package com.attus.financial.service;

import com.attus.financial.domain.entity.Product;
import com.attus.financial.domain.enums.ProductStatus;
import com.attus.financial.dto.request.ProductRequest;
import com.attus.financial.dto.response.ProductResponse;
import com.attus.financial.exception.BusinessException;
import com.attus.financial.exception.ResourceNotFoundException;
import com.attus.financial.repository.AccountRepository;
import com.attus.financial.repository.ProductRepository;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public ProductResponse create(ProductRequest request) {
        log.info("[PRODUCT] Criando produto: {}", request.getName());
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .interestRate(request.getInterestRate())
                .minBalance(request.getMinBalance())
                .status(ProductStatus.ACTIVE)
                .build();
        Product saved = productRepository.save(product);
        auditLogService.log("Product", saved.getId().toString(), "CREATE", saved.getName());
        log.info("[PRODUCT] Produto criado com id: {}", saved.getId());
        return ProductResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        return ProductResponse.from(getProductOrThrow(id));
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        log.info("[PRODUCT] Atualizando produto id: {}", id);
        Product product = getProductOrThrow(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setType(request.getType());
        product.setInterestRate(request.getInterestRate());
        product.setMinBalance(request.getMinBalance());
        Product saved = productRepository.save(product);
        auditLogService.log("Product", id.toString(), "UPDATE", saved.getName());
        return ProductResponse.from(saved);
    }

    @Transactional
    public void toggleStatus(UUID id) {
        Product product = getProductOrThrow(id);
        ProductStatus newStatus = product.getStatus() == ProductStatus.ACTIVE
                ? ProductStatus.INACTIVE : ProductStatus.ACTIVE;
        product.setStatus(newStatus);
        productRepository.save(product);
        auditLogService.log("Product", id.toString(), "STATUS_CHANGE", newStatus.name());
        log.info("[PRODUCT] Status do produto {} alterado para {}", id, newStatus);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = getProductOrThrow(id);
        if (accountRepository.hasAccountsLinkedToProduct(id)) {
            throw new BusinessException("PRODUCT_IN_USE",
                    "Produto possui contas vinculadas e não pode ser excluído",
                    HttpStatus.CONFLICT);
        }
        productRepository.delete(product);
        auditLogService.log("Product", id.toString(), "DELETE", product.getName());
        log.info("[PRODUCT] Produto {} excluído", id);
    }

    private Product getProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", id));
    }
}
