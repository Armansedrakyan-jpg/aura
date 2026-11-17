package com.example.auraclone.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "shop_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_shop_item_code", columnNames = {"code"}),
                @UniqueConstraint(name = "uk_shop_item_gp_product", columnNames = {"google_play_product_id"})
        },
        indexes = {
                @Index(name = "idx_shop_items_active_sort", columnList = "active, sort_order")
        }
)
public class ShopItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ShopItemType type;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ItemRarity rarity;

    @Column(name = "price_credits")
    private Integer priceCredits;

    @Column(name = "google_play_product_id", length = 128)
    private String googlePlayProductId;

    @Column(name = "credits_amount")
    private Integer creditsAmount;

    @Column(nullable = false, columnDefinition = "BOOLEAN NOT NULL DEFAULT TRUE")
    private boolean active = true;

    @Column(name = "sort_order", nullable = false, columnDefinition = "INT NOT NULL DEFAULT 0")
    private int sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ShopItem() {
    }

    public ShopItem(String code, ShopItemType type, String name, String description, ItemRarity rarity,
                    Integer priceCredits, String googlePlayProductId, Integer creditsAmount, int sortOrder) {
        this.code = code;
        this.type = type;
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.priceCredits = priceCredits;
        this.googlePlayProductId = googlePlayProductId;
        this.creditsAmount = creditsAmount;
        this.sortOrder = sortOrder;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ShopItemType getType() {
        return type;
    }

    public void setType(ShopItemType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ItemRarity getRarity() {
        return rarity;
    }

    public void setRarity(ItemRarity rarity) {
        this.rarity = rarity;
    }

    public Integer getPriceCredits() {
        return priceCredits;
    }

    public void setPriceCredits(Integer priceCredits) {
        this.priceCredits = priceCredits;
    }

    public String getGooglePlayProductId() {
        return googlePlayProductId;
    }

    public void setGooglePlayProductId(String googlePlayProductId) {
        this.googlePlayProductId = googlePlayProductId;
    }

    public Integer getCreditsAmount() {
        return creditsAmount;
    }

    public void setCreditsAmount(Integer creditsAmount) {
        this.creditsAmount = creditsAmount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
