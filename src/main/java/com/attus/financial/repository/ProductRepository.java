package com.attus.financial.repository;

import com.attus.financial.domain.entity.Product;
import com.attus.financial.domain.enums.ProductStatus;
import com.attus.financial.domain.enums.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByStatus(ProductStatus status);
    List<Product> findByType(ProductType type);
    boolean existsByNameIgnoreCase(String name);
}
