package com.example.auraclone.service;

import com.example.auraclone.dto.ShopDtos.*;
import com.example.auraclone.entity.*;
import com.example.auraclone.exception.ApiException;
import com.example.auraclone.repository.PurchaseTransactionRepository;
import com.example.auraclone.repository.ShopItemRepository;
import com.example.auraclone.repository.UserInventoryRepository;
import com.example.auraclone.repository.UserRepository;
import com.example.auraclone.service.GooglePlayBillingService.VerificationResult;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ShopService {

    private final ShopItemRepository shopItemRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final PurchaseTransactionRepository purchaseTransactionRepository;
    private final UserRepository userRepository;
    private final GooglePlayBillingService googlePlayBillingService;

    public ShopService(ShopItemRepository shopItemRepository,
                       UserInventoryRepository userInventoryRepository,
                       PurchaseTransactionRepository purchaseTransactionRepository,
                       UserRepository userRepository,
                       GooglePlayBillingService googlePlayBillingService) {
        this.shopItemRepository = shopItemRepository;
        this.userInventoryRepository = userInventoryRepository;
        this.purchaseTransactionRepository = purchaseTransactionRepository;
        this.userRepository = userRepository;
        this.googlePlayBillingService = googlePlayBillingService;
    }

    public ShopCatalogResponse getCatalog(Long userId, ShopItemType typeFilter) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        List<ShopItem> items = typeFilter == null
                ? shopItemRepository.findByActiveTrueOrderBySortOrderAscIdAsc()
                : shopItemRepository.findByActiveTrueAndTypeOrderBySortOrderAscIdAsc(typeFilter);

        Set<Long> ownedItemIds = new HashSet<>(userInventoryRepository.findItemIdsByUserId(userId));

        List<ShopItemResponse> responses = items.stream()
                .map(item -> {
                    boolean isCosmetic = item.getType() == ShopItemType.FRAME || item.getType() == ShopItemType.EFFECT;
                    boolean owned = isCosmetic && ownedItemIds.contains(item.getId());
                    boolean equipped = (item.getType() == ShopItemType.FRAME && item.getId().equals(user.getEquippedFrameId()))
                            || (item.getType() == ShopItemType.EFFECT && item.getId().equals(user.getEquippedEffectId()));
                    return new ShopItemResponse(
                            item.getId(), item.getCode(), item.getType(), item.getName(), item.getDescription(),
                            item.getRarity(), item.getPriceCredits(), item.getGooglePlayProductId(),
                            item.getCreditsAmount(), owned, equipped
                    );
                })
                .toList();

        return new ShopCatalogResponse(
                user.getCredits(), user.getEquippedFrameId(), user.getAvatarFrame(),
                user.getEquippedEffectId(), user.getAvatarEffect(), responses
        );
    }

    @Transactional
    public BuyItemResponse buyItem(Long userId, Long itemId) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        ShopItem item = shopItemRepository.findById(itemId)
                .filter(ShopItem::isActive)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Товар не найден"));

        if (item.getType() == ShopItemType.CREDIT_PACK) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Пакеты кредитов приобретаются через Google Play");
        }
        if (userInventoryRepository.existsByUserIdAndItemId(userId, itemId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Этот предмет уже куплен");
        }

        int price = item.getPriceCredits() == null ? 0 : item.getPriceCredits();
        if (price <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Некорректная цена товара");
        }
        if (user.getCredits() < price) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Недостаточно кредитов");
        }

        user.setCredits(user.getCredits() - price);
        userRepository.save(user);

        try {
            userInventoryRepository.saveAndFlush(new UserInventory(userId, itemId, price));
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(HttpStatus.CONFLICT, "Этот предмет уже куплен");
        }

        return new BuyItemResponse(item.getId(), item.getCode(), item.getType(), user.getCredits(), true);
    }

    @Transactional
    public EquipItemResponse equipItem(Long userId, Long itemId) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        ShopItem item = shopItemRepository.findById(itemId)
                .filter(ShopItem::isActive)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Товар не найден"));

        if (item.getType() == ShopItemType.CREDIT_PACK) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Нельзя надеть пакет кредитов");
        }
        if (!userInventoryRepository.existsByUserIdAndItemId(userId, itemId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Предмет не куплен");
        }

        if (item.getType() == ShopItemType.FRAME) {
            user.setEquippedFrameId(item.getId());
            user.setAvatarFrame(item.getCode());
        } else if (item.getType() == ShopItemType.EFFECT) {
            user.setEquippedEffectId(item.getId());
            user.setAvatarEffect(item.getCode());
        }

        userRepository.save(user);
        return new EquipItemResponse(
                user.getEquippedFrameId(), user.getAvatarFrame(),
                user.getEquippedEffectId(), user.getAvatarEffect()
        );
    }

    @Transactional
    public EquipItemResponse unequip(Long userId, ShopItemType type) {
        if (type == null || type == ShopItemType.CREDIT_PACK) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Можно снять только FRAME или EFFECT");
        }
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        if (type == ShopItemType.FRAME) {
            user.setEquippedFrameId(null);
            user.setAvatarFrame("DEFAULT");
        } else {
            user.setEquippedEffectId(null);
            user.setAvatarEffect(null);
        }

        userRepository.save(user);
        return new EquipItemResponse(
                user.getEquippedFrameId(), user.getAvatarFrame(),
                user.getEquippedEffectId(), user.getAvatarEffect()
        );
    }

    @Transactional
    public VerifyPurchaseResponse verifyGooglePlayPurchase(Long userId, VerifyPurchaseRequest request) {
        String productId = request.productId.trim();
        String purchaseToken = request.purchaseToken.trim();

        ShopItem creditPack = shopItemRepository.findByGooglePlayProductIdAndActiveTrue(productId)
                .filter(item -> item.getType() == ShopItemType.CREDIT_PACK)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Пакет кредитов не найден"));

        int creditsAmount = creditPack.getCreditsAmount() == null ? 0 : creditPack.getCreditsAmount();
        if (creditsAmount <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Некорректный пакет кредитов");
        }
        if (purchaseTransactionRepository.existsByPurchaseToken(purchaseToken)) {
            throw new ApiException(HttpStatus.CONFLICT, "Покупка с таким purchaseToken уже активирована");
        }

        VerificationResult verification = googlePlayBillingService.verifyConsumablePurchase(productId, purchaseToken);
        if (!verification.valid()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    verification.failureReason() != null ? verification.failureReason() : "Ошибка проверки покупки");
        }

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        PurchaseTransaction tx = new PurchaseTransaction(
                userId, productId, purchaseToken, verification.orderId(), creditsAmount, "VERIFIED"
        );
        try {
            tx = purchaseTransactionRepository.saveAndFlush(tx);
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(HttpStatus.CONFLICT, "Покупка с таким purchaseToken уже активирована");
        }

        user.setCredits(user.getCredits() + creditsAmount);
        userRepository.save(user);

        return new VerifyPurchaseResponse(tx.getId(), productId, creditsAmount, user.getCredits(), tx.getStatus());
    }
}
