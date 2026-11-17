package com.example.auraclone.dto;

import com.example.auraclone.entity.ItemRarity;
import com.example.auraclone.entity.ShopItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ShopDtos {

    public static class ShopCatalogResponse {
        public int credits;
        public Long equippedFrameId;
        public String avatarFrame;
        public Long equippedEffectId;
        public String avatarEffect;
        public List<ShopItemResponse> items;

        public ShopCatalogResponse() {
        }

        public ShopCatalogResponse(int credits, Long equippedFrameId, String avatarFrame,
                                   Long equippedEffectId, String avatarEffect, List<ShopItemResponse> items) {
            this.credits = credits;
            this.equippedFrameId = equippedFrameId;
            this.avatarFrame = avatarFrame;
            this.equippedEffectId = equippedEffectId;
            this.avatarEffect = avatarEffect;
            this.items = items;
        }
    }

    public static class ShopItemResponse {
        public Long id;
        public String code;
        public ShopItemType type;
        public String name;
        public String description;
        public ItemRarity rarity;
        public Integer priceCredits;
        public String googlePlayProductId;
        public Integer creditsAmount;
        public boolean owned;
        public boolean equipped;

        public ShopItemResponse() {
        }

        public ShopItemResponse(Long id, String code, ShopItemType type, String name, String description,
                                ItemRarity rarity, Integer priceCredits, String googlePlayProductId,
                                Integer creditsAmount, boolean owned, boolean equipped) {
            this.id = id;
            this.code = code;
            this.type = type;
            this.name = name;
            this.description = description;
            this.rarity = rarity;
            this.priceCredits = priceCredits;
            this.googlePlayProductId = googlePlayProductId;
            this.creditsAmount = creditsAmount;
            this.owned = owned;
            this.equipped = equipped;
        }
    }

    public static class BuyItemResponse {
        public Long itemId;
        public String code;
        public ShopItemType type;
        public int credits;
        public boolean owned;

        public BuyItemResponse(Long itemId, String code, ShopItemType type, int credits, boolean owned) {
            this.itemId = itemId;
            this.code = code;
            this.type = type;
            this.credits = credits;
            this.owned = owned;
        }
    }

    public static class EquipItemResponse {
        public Long equippedFrameId;
        public String avatarFrame;
        public Long equippedEffectId;
        public String avatarEffect;

        public EquipItemResponse(Long equippedFrameId, String avatarFrame,
                                 Long equippedEffectId, String avatarEffect) {
            this.equippedFrameId = equippedFrameId;
            this.avatarFrame = avatarFrame;
            this.equippedEffectId = equippedEffectId;
            this.avatarEffect = avatarEffect;
        }
    }

    public static class VerifyPurchaseRequest {
        @NotBlank(message = "productId обязателен")
        @Size(max = 128, message = "productId слишком длинный")
        public String productId;

        @NotBlank(message = "purchaseToken обязателен")
        @Size(max = 512, message = "purchaseToken слишком длинный")
        public String purchaseToken;
    }

    public static class VerifyPurchaseResponse {
        public Long transactionId;
        public String productId;
        public int creditsAdded;
        public int totalCredits;
        public String status;

        public VerifyPurchaseResponse(Long transactionId, String productId,
                                      int creditsAdded, int totalCredits, String status) {
            this.transactionId = transactionId;
            this.productId = productId;
            this.creditsAdded = creditsAdded;
            this.totalCredits = totalCredits;
            this.status = status;
        }
    }
}
