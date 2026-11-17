package com.example.auraclone.repository;

import com.example.auraclone.entity.PurchaseTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseTransactionRepository extends JpaRepository<PurchaseTransaction, Long> {
    boolean existsByPurchaseToken(String purchaseToken);

    @Modifying
    @Query("UPDATE PurchaseTransaction p SET p.userId = null WHERE p.userId = :userId")
    void detachUser(@Param("userId") Long userId);
}
