package com.example.auraclone;

import com.example.auraclone.entity.AuraAnalysis;
import com.example.auraclone.entity.ShopItemType;
import com.example.auraclone.repository.AuraAnalysisRepository;
import com.example.auraclone.repository.ShopItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ShopIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShopItemRepository shopItemRepository;

    @Autowired
    private AuraAnalysisRepository auraAnalysisRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shopEndpointsRequireAuth() throws Exception {
        mockMvc.perform(get("/api/shop/items")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/shop/buy/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/shop/equip/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/shop/verify-purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"credits_pack_50\",\"purchaseToken\":\"tok_12345678\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void fullShopBuyEquipAndGooglePlayVerifyFlow() throws Exception {
        String regBody = """
                {
                  "username": "shop_tester",
                  "email": "shop_tester@example.com",
                  "password": "secret123"
                }
                """;
        MvcResult regRes = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(regBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode regJson = objectMapper.readTree(regRes.getResponse().getContentAsString());
        String token = regJson.get("token").asText();
        Long userId = regJson.get("userId").asLong();

        auraAnalysisRepository.save(new AuraAnalysis(
                userId, 95, "Легенда", "Космос", "Макс", "Железная", "Лидер", "Топ аура"
        ));

        Long frameId = shopItemRepository.findByCodeIgnoreCaseAndTypeAndActiveTrue("GOLD", ShopItemType.FRAME)
                .orElseThrow().getId();
        Long effectId = shopItemRepository.findByCodeIgnoreCaseAndTypeAndActiveTrue("AURA_SPARKS", ShopItemType.EFFECT)
                .orElseThrow().getId();

        mockMvc.perform(get("/api/shop/items").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credits").value(0))
                .andExpect(jsonPath("$.items.length()", greaterThanOrEqualTo(10)))
                .andExpect(jsonPath("$.items[*].code", hasItem("GOLD")))
                .andExpect(jsonPath("$.items[*].code", hasItem("AURA_SPARKS")));

        mockMvc.perform(post("/api/shop/buy/" + frameId).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/shop/equip/" + frameId).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/shop/verify-purchase")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"credits_pack_150\",\"purchaseToken\":\"INVALID\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/shop/verify-purchase")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"credits_pack_150\",\"purchaseToken\":\"gp_token_valid_0001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creditsAdded").value(150))
                .andExpect(jsonPath("$.totalCredits").value(150))
                .andExpect(jsonPath("$.status").value("VERIFIED"));

        mockMvc.perform(post("/api/shop/verify-purchase")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"credits_pack_150\",\"purchaseToken\":\"gp_token_valid_0001\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/shop/verify-purchase")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"credits_pack_50\",\"purchaseToken\":\"gp_token_valid_0002\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCredits").value(200));

        mockMvc.perform(post("/api/shop/buy/" + frameId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credits").value(50))
                .andExpect(jsonPath("$.owned").value(true));

        mockMvc.perform(post("/api/shop/buy/" + frameId).header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/shop/buy/" + effectId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credits").value(10));

        mockMvc.perform(post("/api/shop/equip/" + frameId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equippedFrameId").value(frameId))
                .andExpect(jsonPath("$.avatarFrame").value("GOLD"));

        mockMvc.perform(post("/api/shop/equip/" + effectId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equippedEffectId").value(effectId))
                .andExpect(jsonPath("$.avatarEffect").value("AURA_SPARKS"));

        mockMvc.perform(get("/api/shop/items").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credits").value(10))
                .andExpect(jsonPath("$.equippedFrameId").value(frameId))
                .andExpect(jsonPath("$.equippedEffectId").value(effectId));

        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarFrame").value("GOLD"))
                .andExpect(jsonPath("$.avatarEffect").value("AURA_SPARKS"))
                .andExpect(jsonPath("$.equippedFrameId").value(frameId))
                .andExpect(jsonPath("$.equippedEffectId").value(effectId));

        mockMvc.perform(get("/api/aura/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.userId == " + userId + ")].avatarFrame", hasItem("GOLD")))
                .andExpect(jsonPath("$[?(@.userId == " + userId + ")].avatarEffect", hasItem("AURA_SPARKS")));

        // 8. Delete account keeps purchase token blocked against reuse
        mockMvc.perform(delete("/api/auth/account").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Re-register and try to reuse old purchaseToken -> still rejected with 409
        MvcResult reg2Res = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "shop_tester_2",
                                  "email": "shop_tester_2@example.com",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String token2 = objectMapper.readTree(reg2Res.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(post("/api/shop/verify-purchase")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"credits_pack_150\",\"purchaseToken\":\"gp_token_valid_0001\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/auth/account").header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk());
    }
}
