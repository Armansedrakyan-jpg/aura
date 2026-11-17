package com.example.auraclone.controller;

import com.example.auraclone.dto.PublicUserProfileResponse;
import com.example.auraclone.dto.PublicUserProfileResponse.UpdateFrameRequest;
import com.example.auraclone.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicUserProfileResponse> getPublicProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userProfileService.getPublicProfile(id));
    }

    @PutMapping("/me/frame")
    public ResponseEntity<Map<String, String>> updateAvatarFrame(
            @Valid @RequestBody UpdateFrameRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        String updatedFrame = userProfileService.updateAvatarFrame(userId, request.avatarFrame);
        return ResponseEntity.ok(Map.of("avatarFrame", updatedFrame));
    }
}
