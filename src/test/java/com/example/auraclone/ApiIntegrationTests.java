package com.example.auraclone;

import com.example.auraclone.dto.AuraAnalysisResult;
import com.example.auraclone.entity.BattleHistory;
import com.example.auraclone.entity.User;
import com.example.auraclone.repository.BattleHistoryRepository;
import com.example.auraclone.repository.UserRepository;
import com.example.auraclone.service.GeminiAuraService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BattleHistoryRepository battleHistoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private GeminiAuraService geminiAuraService;

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void protectedEndpointsReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/aura/history"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(get("/api/aura/cooldown"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/battles/start"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void fullAuthAnalyzeHistoryLeaderboardAndDeleteFlow() throws Exception {
        when(geminiAuraService.analyze(any(), eq("image/jpeg"), eq("люблю код")))
                .thenReturn(new AuraAnalysisResult(88, "Кодер Сигма", "Спокойный", "Высокая", "Железная", "Умный", "Топ аура"));

        String regBody = """
                {
                  "username": "arman_dev",
                  "email": "Arman@Example.com",
                  "password": "secret123"
                }
                """;

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(regBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("arman_dev"))
                .andReturn();

        JsonNode regJson = objectMapper.readTree(regResult.getResponse().getContentAsString());
        String token = regJson.get("token").asText();
        Long userId = regJson.get("userId").asLong();

        // Duplicate username returns 409
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(regBody))
                .andExpect(status().isConflict());

        // Login with uppercase email works
        String loginBody = """
                {
                  "usernameOrEmail": "arman@example.com",
                  "password": "secret123"
                }
                """;
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        // Initially free generation is available
        mockMvc.perform(get("/api/aura/cooldown")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canGenerateFree").value(true))
                .andExpect(jsonPath("$.remainingSeconds").value(0));

        // Analyze with valid JPEG magic bytes
        byte[] fakeJpeg = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10};
        MockMultipartFile photo = new MockMultipartFile("photo", "selfie.jpg", "image/jpeg", fakeJpeg);

        mockMvc.perform(multipart("/api/aura/analyze")
                        .file(photo)
                        .param("aboutMe", "люблю код")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(88))
                .andExpect(jsonPath("$.level").value("Кодер Сигма"));

        // Cooldown is active now
        mockMvc.perform(get("/api/aura/cooldown")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canGenerateFree").value(false))
                .andExpect(jsonPath("$.remainingSeconds", greaterThan(0)))
                .andExpect(jsonPath("$.nextFreeAt").isNotEmpty());

        // Immediate repeat analyze is blocked with 429 and remainingSeconds
        mockMvc.perform(multipart("/api/aura/analyze")
                        .file(photo)
                        .param("aboutMe", "люблю код")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.remainingSeconds", greaterThan(0)))
                .andExpect(jsonPath("$.nextFreeAt").isNotEmpty())
                .andExpect(jsonPath("$.credits").value(0));

        // Non-image rejected
        MockMultipartFile badFile = new MockMultipartFile("photo", "hack.txt", "text/plain", "not an image".getBytes());
        mockMvc.perform(multipart("/api/aura/analyze")
                        .file(badFile)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        // Grant 1 credit -> failed Gemini rolls credit back, then higher score succeeds
        User user = userRepository.findById(userId).orElseThrow();
        user.setCredits(1);
        userRepository.save(user);

        when(geminiAuraService.analyze(any(), eq("image/jpeg"), eq("fail")))
                .thenThrow(new RuntimeException("Gemini down"));

        mockMvc.perform(multipart("/api/aura/analyze")
                        .file(photo)
                        .param("aboutMe", "fail")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());

        assertEquals(1, userRepository.findById(userId).orElseThrow().getCredits());

        when(geminiAuraService.analyze(any(), eq("image/jpeg"), eq("вторая попытка")))
                .thenReturn(new AuraAnalysisResult(96, "Легенда", "Космос", "Макс", "Абсолют", "Гений", "Лучшая аура"));

        mockMvc.perform(multipart("/api/aura/analyze")
                        .file(photo)
                        .param("aboutMe", "вторая попытка")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(96));

        assertEquals(0, userRepository.findById(userId).orElseThrow().getCredits());

        // History contains 2 analyses
        mockMvc.perform(get("/api/aura/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // Update avatar frame
        mockMvc.perform(put("/api/users/me/frame")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"avatarFrame\":\"neon\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarFrame").value("NEON"));

        // Leaderboard is deduplicated: only 1 row for arman_dev with best score 96
        mockMvc.perform(get("/api/aura/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[0].username").value("arman_dev"))
                .andExpect(jsonPath("$[0].avatarFrame").value("NEON"))
                .andExpect(jsonPath("$[0].score").value(96));

        // Add a battle record and inspect public profile without auth token
        battleHistoryRepository.save(new BattleHistory(userId, "arman_dev", 96, 999L, "opponent", 70, userId));

        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.username").value("arman_dev"))
                .andExpect(jsonPath("$.avatarFrame").value("NEON"))
                .andExpect(jsonPath("$.bestScore").value(96))
                .andExpect(jsonPath("$.bestLevel").value("Легенда"))
                .andExpect(jsonPath("$.averageScore").value(92.0))
                .andExpect(jsonPath("$.totalAnalyses").value(2))
                .andExpect(jsonPath("$.totalBattles").value(1))
                .andExpect(jsonPath("$.winsCount").value(1))
                .andExpect(jsonPath("$.achievements[*].code", hasItem("FIRST_ANALYSIS")))
                .andExpect(jsonPath("$.achievements[*].code", hasItem("SCORE_95_PLUS")))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.credits").doesNotExist())
                .andExpect(jsonPath("$.lastFreeAnalysisAt").doesNotExist());

        mockMvc.perform(get("/api/users/999999"))
                .andExpect(status().isNotFound());

        // Account deletion works and invalidates old token
        mockMvc.perform(delete("/api/auth/account")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("deleted"));

        mockMvc.perform(get("/api/aura/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
