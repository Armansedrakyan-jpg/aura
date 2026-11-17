package com.example.auraclone.service;

import com.example.auraclone.entity.ItemRarity;
import com.example.auraclone.entity.ShopItem;
import com.example.auraclone.entity.ShopItemType;
import com.example.auraclone.repository.ShopItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ShopCatalogSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ShopCatalogSeeder.class);

    private final ShopItemRepository shopItemRepository;

    public ShopCatalogSeeder(ShopItemRepository shopItemRepository) {
        this.shopItemRepository = shopItemRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (shopItemRepository.count() > 0) {
            return;
        }

        List<ShopItem> defaults = List.of(
                new ShopItem("NEON", ShopItemType.FRAME, "Неоновый импульс",
                        "Яркая кибер-рамка с голубым и пурпурным свечением",
                        ItemRarity.RARE, 50, null, null, 10),
                new ShopItem("CYBER", ShopItemType.FRAME, "Киберпанк 2099",
                        "Хромированная голографическая рамка с глитч-гранями",
                        ItemRarity.EPIC, 120, null, null, 20),
                new ShopItem("GOLD", ShopItemType.FRAME, "Золотая корона",
                        "Роскошная золотая рамка для обладателей топовой ауры",
                        ItemRarity.EPIC, 150, null, null, 30),
                new ShopItem("SIGMA", ShopItemType.FRAME, "Сигма Монолит",
                        "Тёмно-обсидиановая рамка с ледяным контуром",
                        ItemRarity.RARE, 80, null, null, 40),
                new ShopItem("LEGENDARY", ShopItemType.FRAME, "Призма Вечности",
                        "Переливающаяся легендарная рамка с космическим спектром",
                        ItemRarity.LEGENDARY, 300, null, null, 50),

                new ShopItem("AURA_SPARKS", ShopItemType.EFFECT, "Искры ауры",
                        "Лёгкие парящие частицы вокруг вашего профиля",
                        ItemRarity.COMMON, 40, null, null, 110),
                new ShopItem("GLITCH_PULSE", ShopItemType.EFFECT, "Глитч-импульс",
                        "Цифровое искажение и вспышки в стиле матрицы",
                        ItemRarity.RARE, 90, null, null, 120),
                new ShopItem("SIGMA_LIGHTNING", ShopItemType.EFFECT, "Молнии Сигмы",
                        "Электрические разряды вокруг карточки в лидерборде",
                        ItemRarity.EPIC, 180, null, null, 130),
                new ShopItem("GALAXY_HALO", ShopItemType.EFFECT, "Галактический нимб",
                        "Сияние звёздной туманности и эффект левитации",
                        ItemRarity.LEGENDARY, 350, null, null, 140),

                new ShopItem("CREDITS_PACK_50", ShopItemType.CREDIT_PACK, "Стартовый набор (50 кредитов)",
                        "50 кредитов для покупок в магазине и дополнительных сканов",
                        ItemRarity.COMMON, null, "credits_pack_50", 50, 210),
                new ShopItem("CREDITS_PACK_150", ShopItemType.CREDIT_PACK, "Пакет Сигмы (150 кредитов)",
                        "150 кредитов по выгодной цене",
                        ItemRarity.RARE, null, "credits_pack_150", 150, 220),
                new ShopItem("CREDITS_PACK_500", ShopItemType.CREDIT_PACK, "Легендарный сундук (500 кредитов)",
                        "500 кредитов для полной коллекции рамок и эффектов",
                        ItemRarity.LEGENDARY, null, "credits_pack_500", 500, 230)
        );

        shopItemRepository.saveAll(defaults);
        log.info("Seeded {} default shop items", defaults.size());
    }
}
