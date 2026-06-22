package com.attus.financial.repository;

import com.attus.financial.domain.entity.Account;
import com.attus.financial.domain.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByCustomerId(UUID customerId);
    List<Account> findByStatus(AccountStatus status);
    boolean existsByCustomerIdAndProductId(UUID customerId, UUID productId);

    @Query("SELECT COUNT(a) > 0 FROM Account a WHERE a.product.id = :productId")
    boolean hasAccountsLinkedToProduct(UUID productId);
}
