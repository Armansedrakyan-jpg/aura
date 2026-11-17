package com.example.auraclone.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_inventory",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_inventory_user_item", columnNames = {"user_id", "item_id"})
        },
        indexes = {
                @Index(name = "idx_user_inventory_user", columnList = "user_id")
        }
)
public class UserInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "price_paid_credits", nullable = false)
    private int pricePaidCredits;

    @Column(name = "purchased_at", nullable = false, updatable = false)
    private LocalDateTime purchasedAt = LocalDateTime.now();

    public UserInventory() {
    }

    public UserInventory(Long userId, Long itemId, int pricePaidCredits) {
        this.userId = userId;
        this.itemId = itemId;
        this.pricePaidCredits = pricePaidCredits;
        this.purchasedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getItemId() {
        return itemId;
    }

    public int getPricePaidCredits() {
        return pricePaidCredits;
    }

    public LocalDateTime getPurchasedAt() {
        return purchasedAt;
    }
}
