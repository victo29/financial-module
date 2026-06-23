package com.attus.financial.service;

import com.attus.financial.domain.entity.Product;
import com.attus.financial.domain.enums.ProductStatus;
import com.attus.financial.domain.enums.ProductType;
import com.attus.financial.dto.request.ProductRequest;
import com.attus.financial.dto.response.ProductResponse;
import com.attus.financial.exception.BusinessException;
import com.attus.financial.exception.ResourceNotFoundException;
import com.attus.financial.repository.AccountRepository;
import com.attus.financial.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service")
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private AuditLogService auditLogService;
    @InjectMocks private ProductService productService;

    private Product buildActiveProduct(UUID id, String name) {
        return Product.builder()
                .id(id).name(name).type(ProductType.SAVINGS)
                .minBalance(BigDecimal.ZERO).status(ProductStatus.ACTIVE).build();
    }

    private ProductRequest buildProductRequest(String name) {
        ProductRequest req = new ProductRequest();
        req.setName(name);
        req.setType(ProductType.SAVINGS);
        req.setInterestRate(new BigDecimal("0.50"));
        req.setMinBalance(BigDecimal.ZERO);
        return req;
    }

    // ─── CREATE ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given a valid product payload")
    class GivenValidProductPayload {

        @Test
        @DisplayName("When the product is created, then it should be persisted with ACTIVE status")
        void whenProductIsCreated_thenItShouldBePersistedWithActiveStatus() {
            ProductRequest request = buildProductRequest("Poupança Premium");
            Product saved = buildActiveProduct(UUID.randomUUID(), "Poupança Premium");

            when(productRepository.save(any(Product.class))).thenReturn(saved);

            ProductResponse response = productService.create(request);

            assertThat(response.getStatus()).isEqualTo(ProductStatus.ACTIVE);
            assertThat(response.getName()).isEqualTo("Poupança Premium");
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("When the product is created, then an audit log CREATE entry should be recorded")
        void whenProductIsCreated_thenAuditLogCreateEntryShouldBeRecorded() {
            Product saved = buildActiveProduct(UUID.randomUUID(), "Produto Auditado");
            when(productRepository.save(any(Product.class))).thenReturn(saved);

            productService.create(buildProductRequest("Produto Auditado"));

            verify(auditLogService).log(eq("Product"), any(), eq("CREATE"), any());
        }
    }

    // ─── FIND ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given multiple products in the repository")
    class GivenMultipleProductsInRepository {

        @Test
        @DisplayName("When findAll is called, then all products should be returned")
        void whenFindAllCalled_thenAllProductsShouldBeReturned() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            when(productRepository.findAll()).thenReturn(List.of(
                    buildActiveProduct(id1, "Produto A"),
                    buildActiveProduct(id2, "Produto B")
            ));

            List<ProductResponse> result = productService.findAll();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(ProductResponse::getName).containsExactly("Produto A", "Produto B");
        }
    }

    @Nested
    @DisplayName("Given an existing product ID")
    class GivenExistingProductId {

        @Test
        @DisplayName("When findById is called, then the matching product should be returned")
        void whenFindByIdCalled_thenMatchingProductShouldBeReturned() {
            UUID id = UUID.randomUUID();
            Product product = buildActiveProduct(id, "Produto Existente");
            when(productRepository.findById(id)).thenReturn(Optional.of(product));

            ProductResponse response = productService.findById(id);

            assertThat(response.getId()).isEqualTo(id);
            assertThat(response.getName()).isEqualTo("Produto Existente");
        }
    }

    @Nested
    @DisplayName("Given a non-existent product ID")
    class GivenNonExistentProductId {

        @Test
        @DisplayName("When findById is called, then ResourceNotFoundException should be thrown")
        void whenFindByIdCalled_thenResourceNotFoundExceptionShouldBeThrown() {
            UUID nonExistentId = UUID.randomUUID();
            when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.findById(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Produto");
        }
    }

    // ─── UPDATE ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given an existing product and a valid update payload")
    class GivenExistingProductAndValidUpdatePayload {

        @Test
        @DisplayName("When the product is updated, then all fields should reflect the new data")
        void whenProductIsUpdated_thenAllFieldsShouldReflectNewData() {
            UUID id = UUID.randomUUID();
            Product existing = buildActiveProduct(id, "Produto Original");
            ProductRequest update = new ProductRequest();
            update.setName("Produto Atualizado");
            update.setType(ProductType.CHECKING);
            update.setMinBalance(new BigDecimal("100.00"));

            when(productRepository.findById(id)).thenReturn(Optional.of(existing));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

            ProductResponse response = productService.update(id, update);

            assertThat(response.getName()).isEqualTo("Produto Atualizado");
            assertThat(response.getMinBalance()).isEqualByComparingTo("100.00");
            verify(auditLogService).log(eq("Product"), any(), eq("UPDATE"), any());
        }
    }

    // ─── TOGGLE STATUS ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given an ACTIVE product")
    class GivenActiveProduct {

        @Test
        @DisplayName("When toggleStatus is called, then status should transition to INACTIVE")
        void whenToggleStatusCalled_thenStatusShouldTransitionToInactive() {
            UUID id = UUID.randomUUID();
            Product product = buildActiveProduct(id, "Produto Ativo");
            when(productRepository.findById(id)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

            productService.toggleStatus(id);

            assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
            verify(auditLogService).log(eq("Product"), any(), eq("STATUS_CHANGE"), eq("INACTIVE"));
        }
    }

    @Nested
    @DisplayName("Given an INACTIVE product")
    class GivenInactiveProduct {

        @Test
        @DisplayName("When toggleStatus is called, then status should transition to ACTIVE")
        void whenToggleStatusCalled_thenStatusShouldTransitionToActive() {
            UUID id = UUID.randomUUID();
            Product product = buildActiveProduct(id, "Produto Inativo");
            product.setStatus(ProductStatus.INACTIVE);
            when(productRepository.findById(id)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

            productService.toggleStatus(id);

            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
            verify(auditLogService).log(eq("Product"), any(), eq("STATUS_CHANGE"), eq("ACTIVE"));
        }
    }

    // ─── DELETE ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Given a product with linked accounts")
    class GivenProductWithLinkedAccounts {

        @Test
        @DisplayName("When delete is attempted, then BusinessException with code PRODUCT_IN_USE should be thrown")
        void whenDeleteAttempted_thenProductInUseExceptionShouldBeThrown() {
            UUID id = UUID.randomUUID();
            when(productRepository.findById(id)).thenReturn(Optional.of(buildActiveProduct(id, "Produto Vinculado")));
            when(accountRepository.hasAccountsLinkedToProduct(id)).thenReturn(true);

            assertThatThrownBy(() -> productService.delete(id))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("contas vinculadas");

            verify(productRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Given a product with no linked accounts")
    class GivenProductWithNoLinkedAccounts {

        @Test
        @DisplayName("When delete is called, then the product should be removed and audit log DELETE entry recorded")
        void whenDeleteCalled_thenProductShouldBeRemovedAndAuditLogged() {
            UUID id = UUID.randomUUID();
            Product product = buildActiveProduct(id, "Produto Livre");
            when(productRepository.findById(id)).thenReturn(Optional.of(product));
            when(accountRepository.hasAccountsLinkedToProduct(id)).thenReturn(false);

            productService.delete(id);

            verify(productRepository).delete(product);
            verify(auditLogService).log(eq("Product"), any(), eq("DELETE"), any());
        }
    }
}
