package com.example.auraclone.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "purchase_transactions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_purchase_token", columnNames = {"purchase_token"})
        },
        indexes = {
                @Index(name = "idx_purchase_user", columnList = "user_id")
        }
)
public class PurchaseTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "product_id", nullable = false, length = 128)
    private String productId;

    @Column(name = "purchase_token", nullable = false, length = 512)
    private String purchaseToken;

    @Column(name = "order_id", length = 128)
    private String orderId;

    @Column(name = "credits_granted", nullable = false)
    private int creditsGranted;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PurchaseTransaction() {
    }

    public PurchaseTransaction(Long userId, String productId, String purchaseToken,
                               String orderId, int creditsGranted, String status) {
        this.userId = userId;
        this.productId = productId;
        this.purchaseToken = purchaseToken;
        this.orderId = orderId;
        this.creditsGranted = creditsGranted;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getProductId() {
        return productId;
    }

    public String getPurchaseToken() {
        return purchaseToken;
    }

    public String getOrderId() {
        return orderId;
    }

    public int getCreditsGranted() {
        return creditsGranted;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
