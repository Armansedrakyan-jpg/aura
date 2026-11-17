package com.example.auraclone.repository;

import com.example.auraclone.entity.ShopItem;
import com.example.auraclone.entity.ShopItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {
    List<ShopItem> findByActiveTrueOrderBySortOrderAscIdAsc();
    List<ShopItem> findByActiveTrueAndTypeOrderBySortOrderAscIdAsc(ShopItemType type);
    Optional<ShopItem> findByGooglePlayProductIdAndActiveTrue(String googlePlayProductId);
    Optional<ShopItem> findByCodeIgnoreCaseAndTypeAndActiveTrue(String code, ShopItemType type);
}
