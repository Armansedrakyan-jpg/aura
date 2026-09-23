package com.example.auraclone.controller;

import com.example.auraclone.dto.Battle;
import com.example.auraclone.dto.Player;
import com.example.auraclone.service.BattleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/battles")
public class BattleController {

    private final BattleService battleService;

    public BattleController(BattleService battleService) {
        this.battleService = battleService;
    }

    @PostMapping("/start")
    public ResponseEntity<?> startBattle(@RequestBody Player player) {
        Battle newBattle = new Battle("WAITING", System.currentTimeMillis(), player);
        String battleId = battleService.startBattle(newBattle);

        // Возвращаем JSON-объект вместо обычного текста!
        return ResponseEntity.ok(java.util.Map.of("battleId", battleId));
    }
    @PutMapping("/join/{battleId}")
    public ResponseEntity<?> joinBattle(@PathVariable String battleId, @RequestBody Player player2) {
        battleService.joinBattle(battleId, player2);
        // Возвращаем JSON-карту вместо обычного текста!
        return ResponseEntity.ok(java.util.Map.of("status", "success"));
    }

}