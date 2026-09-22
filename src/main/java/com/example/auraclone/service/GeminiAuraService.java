package com.example.auraclone.service;

import com.example.auraclone.dto.AuraAnalysisResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class GeminiAuraService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String apiKey;

    // Можно менять модель на gemini-1.5-pro при необходимости
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=%s";

    public GeminiAuraService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Отправляет фото + описание пользователя в Gemini,
     * получает и парсит JSON с результатом анализа "ауры".
     */
    public AuraAnalysisResult analyze(byte[] photoBytes, String aboutMe) {
        String base64Image = Base64.getEncoder().encodeToString(photoBytes);

        String prompt = buildPrompt(aboutMe);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt),
                                Map.of("inline_data", Map.of(
                                        "mime_type", "image/jpeg",
                                        "data", base64Image
                                ))
                        ))
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String url = String.format(GEMINI_URL, apiKey);
        String rawResponse = restTemplate.postForObject(url, entity, String.class);

        return parseGeminiResponse(rawResponse);
    }

    private String buildPrompt(String aboutMe) {
        return """
            Ты — приложение, которое в развлекательном и юмористическом формате анализирует "ауру" человека
            по фото и его описанию себя. Это лёгкий, позитивный и вирусный контент для соцсетей.

            Описание пользователя о себе: "%s"

            Твоя задача — проанализировать фото и описание и вернуть СТРОГО валидный JSON без каких-либо пояснений и без markdown-разметки (не оборачивай в ```json ... ```) в следующем формате:
            {
              "score": <число от 10 до 100. Распределяй баллы контрастно, не завышай всем одинаково (например, не ставь всем 95-96)>,
              "level": "<придумай уникальный, смешной молодежный титул из 2-4 слов в стиле трендов TikTok, который идеально подходит под фото и описание, например: 'Уставший Сигма', 'Защитник пельменей', 'GigaChad кодинга', 'Гроза энерготоников', 'Скрытый NPC'>",
              "vibe": "<короткая характеристика вайба>",
              "energy": "<характеристика энергии>",
              "confidence": "<уровень уверенности>",
              "personality": "<пара слов о личности>",
              "verdict": "<короткий позитивный итоговый вывод>"
            }
            """.formatted(aboutMe == null ? "" : aboutMe);
    }

    private AuraAnalysisResult parseGeminiResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String text = root
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // На случай если модель обернула JSON в ```json ... ```
            String cleaned = text.replace("```json", "").replace("```", "").trim();

            JsonNode resultNode = objectMapper.readTree(cleaned);

            return new AuraAnalysisResult(
                    resultNode.path("score").asInt(50),
                    resultNode.path("level").asText("MID"),
                    resultNode.path("vibe").asText(""),
                    resultNode.path("energy").asText(""),
                    resultNode.path("confidence").asText(""),
                    resultNode.path("personality").asText(""),
                    resultNode.path("verdict").asText("")
            );
        } catch (Exception e) {
            // Фолбэк на случай сбоя парсинга — чтобы приложение не падало на демо
            return new AuraAnalysisResult(
                    50, "MID", "Неопределённый вайб", "Средняя",
                    "Средняя", "Загадочная личность",
                    "Не удалось точно проанализировать, попробуй другое фото"
            );
        }
    }
}
