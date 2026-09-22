package com.example.auraclone.controller;

import com.example.auraclone.dto.AuraAnalysisResult;
import com.example.auraclone.entity.AuraAnalysis;
import com.example.auraclone.repository.AuraAnalysisRepository;
import com.example.auraclone.service.GeminiAuraService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/aura")
public class AuraAnalyzeController {

    private final GeminiAuraService geminiAuraService;
    private final AuraAnalysisRepository auraAnalysisRepository;

    public AuraAnalyzeController(GeminiAuraService geminiAuraService,
                                  AuraAnalysisRepository auraAnalysisRepository) {
        this.geminiAuraService = geminiAuraService;
        this.auraAnalysisRepository = auraAnalysisRepository;
    }

    /**
     * Требует заголовок: Authorization: Bearer <токен из /api/auth/login или /register>
     */
    @PostMapping(value = "/analyze", consumes = "multipart/form-data")
    public ResponseEntity<?> analyze(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(value = "aboutMe", required = false) String aboutMe,
            Authentication authentication
    ) {
        try {
            Long userId = (Long) authentication.getPrincipal();

            AuraAnalysisResult result = geminiAuraService.analyze(photo.getBytes(), aboutMe);

            AuraAnalysis entity = new AuraAnalysis(
                    userId,
                    result.getScore(),
                    result.getLevel(),
                    result.getVibe(),
                    result.getEnergy(),
                    result.getConfidence(),
                    result.getPersonality(),
                    result.getVerdict()
            );
            AuraAnalysis saved = auraAnalysisRepository.save(entity);

            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Ошибка чтения фото");
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<AuraAnalysis>> history(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(auraAnalysisRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();

        return auraAnalysisRepository.findById(id)
                .filter(a -> a.getUserId().equals(userId))
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body("Не найдено"));
    }
}
