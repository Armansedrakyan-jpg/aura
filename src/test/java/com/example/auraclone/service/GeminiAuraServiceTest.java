package com.example.auraclone.service;

import com.example.auraclone.dto.AuraAnalysisResult;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

class GeminiAuraServiceTest {

    private final GeminiAuraService service = new GeminiAuraService(new RestTemplate());

    @Test
    void parsesValidGeminiJsonAndClampsScore() {
        String raw = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "```json\\n{\\"score\\": 140, \\"level\\": \\"GigaChad\\", \\"vibe\\": \\"Топ\\", \\"energy\\": \\"Мощная\\", \\"confidence\\": \\"100%\\", \\"personality\\": \\"Лидер\\", \\"verdict\\": \\"Огонь\\"}\\n```"
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        AuraAnalysisResult result = service.parseGeminiResponse(raw);
        assertEquals(100, result.getScore());
        assertEquals("GigaChad", result.getLevel());
        assertEquals("Топ", result.getVibe());
        assertEquals("Огонь", result.getVerdict());
    }

    @Test
    void returnsFallbackWhenResponseIsInvalid() {
        AuraAnalysisResult result = service.parseGeminiResponse("{broken json");
        assertEquals(50, result.getScore());
        assertEquals("MID", result.getLevel());
    }
}
