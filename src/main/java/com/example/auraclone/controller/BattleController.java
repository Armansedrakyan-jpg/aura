package com.example.auraclone.controller;

import com.example.auraclone.dto.Battle;
import com.example.auraclone.dto.Player;
import com.example.auraclone.entity.BattleHistory;
import com.example.auraclone.service.BattleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/battles")
public class BattleController {

    private final BattleService battleService;

    public BattleController(BattleService battleService) {
        this.battleService = battleService;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startBattle(
            @RequestBody(required = false) Player player,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        String battleId = battleService.startBattle(userId, player);
        return ResponseEntity.ok(Map.of("battleId", battleId));
    }

    @PutMapping("/join/{battleId}")
    public ResponseEntity<Battle> joinBattle(
            @PathVariable String battleId,
            @RequestBody(required = false) Player player2,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        Battle finished = battleService.joinBattle(battleId, userId, player2);
        return ResponseEntity.ok(finished);
    }

    @GetMapping("/history")
    public ResponseEntity<List<BattleHistory>> history(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(battleService.getUserBattleHistory(userId));
    }
}
