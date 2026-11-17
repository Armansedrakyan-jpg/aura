package com.example.auraclone.controller;

import com.example.auraclone.dto.ShopDtos.*;
import com.example.auraclone.entity.ShopItemType;
import com.example.auraclone.service.ShopService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shop")
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping("/items")
    public ResponseEntity<ShopCatalogResponse> getItems(
            @RequestParam(value = "type", required = false) ShopItemType type,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(shopService.getCatalog(userId, type));
    }

    @PostMapping("/buy/{itemId}")
    public ResponseEntity<BuyItemResponse> buyItem(
            @PathVariable Long itemId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(shopService.buyItem(userId, itemId));
    }

    @PostMapping("/equip/{itemId}")
    public ResponseEntity<EquipItemResponse> equipItem(
            @PathVariable Long itemId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(shopService.equipItem(userId, itemId));
    }

    @PostMapping("/unequip/{type}")
    public ResponseEntity<EquipItemResponse> unequip(
            @PathVariable ShopItemType type,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(shopService.unequip(userId, type));
    }

    @PostMapping("/verify-purchase")
    public ResponseEntity<VerifyPurchaseResponse> verifyPurchase(
            @Valid @RequestBody VerifyPurchaseRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(shopService.verifyGooglePlayPurchase(userId, request));
    }
}
