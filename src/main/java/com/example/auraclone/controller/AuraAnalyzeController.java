package com.example.auraclone.controller;

import com.example.auraclone.dto.CooldownStatusResponse;
import com.example.auraclone.dto.LeaderboardEntryResponse;
import com.example.auraclone.entity.AuraAnalysis;
import com.example.auraclone.exception.ApiException;
import com.example.auraclone.repository.AuraAnalysisRepository;
import com.example.auraclone.service.AuraAnalysisService;
import com.example.auraclone.service.AuraQuotaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/aura")
public class AuraAnalyzeController {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private final AuraAnalysisService auraAnalysisService;
    private final AuraQuotaService auraQuotaService;
    private final AuraAnalysisRepository auraAnalysisRepository;

    public AuraAnalyzeController(AuraAnalysisService auraAnalysisService,
                                 AuraQuotaService auraQuotaService,
                                 AuraAnalysisRepository auraAnalysisRepository) {
        this.auraAnalysisService = auraAnalysisService;
        this.auraQuotaService = auraQuotaService;
        this.auraAnalysisRepository = auraAnalysisRepository;
    }

    @PostMapping(value = "/analyze", consumes = "multipart/form-data")
    public ResponseEntity<AuraAnalysis> analyze(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(value = "aboutMe", required = false) String aboutMe,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        if (photo == null || photo.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Фото не должно быть пустым");
        }

        byte[] photoBytes;
        try {
            photoBytes = photo.getBytes();
        } catch (IOException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ошибка чтения фото");
        }

        String detectedMime = detectImageMimeType(photoBytes, photo.getContentType());
        if (detectedMime == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Поддерживаются только изображения JPEG, PNG или WebP");
        }

        AuraAnalysis saved = auraAnalysisService.analyzeAndSave(userId, photoBytes, detectedMime, aboutMe);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/cooldown")
    public ResponseEntity<CooldownStatusResponse> getCooldownStatus(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(auraQuotaService.getCooldownStatus(userId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<AuraAnalysis>> history(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(auraAnalysisRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuraAnalysis> getOne(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();

        return auraAnalysisRepository.findById(id)
                .filter(a -> a.getUserId().equals(userId))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Анализ не найден"));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryResponse>> getLeaderboard() {
        return ResponseEntity.ok(auraAnalysisService.getLeaderboard(100));
    }

    private String detectImageMimeType(byte[] bytes, String declaredContentType) {
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47
                && bytes[4] == 0x0D && bytes[5] == 0x0A && bytes[6] == 0x1A && bytes[7] == 0x0A) {
            return "image/png";
        }
        if (bytes.length >= 12
                && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return "image/webp";
        }
        if (declaredContentType != null && ALLOWED_MIME_TYPES.contains(declaredContentType.toLowerCase())) {
            return declaredContentType.equalsIgnoreCase("image/jpg") ? "image/jpeg" : declaredContentType.toLowerCase();
        }
        return null;
    }
}

