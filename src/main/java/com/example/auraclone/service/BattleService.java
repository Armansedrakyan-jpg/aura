package com.example.auraclone.service;

// Импортируем наши классы моделей и сущностей
import com.example.auraclone.dto.Battle;
import com.example.auraclone.dto.Player;
import com.example.auraclone.entity.BattleHistory;
import com.example.auraclone.repository.BattleHistoryRepository;

// Импортируем библиотеки Firebase
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class BattleService {

    private final BattleHistoryRepository battleHistoryRepository;

    // Внедряем JPA репозиторий через конструктор
    public BattleService(BattleHistoryRepository battleHistoryRepository) {
        this.battleHistoryRepository = battleHistoryRepository;
    }

    public String startBattle(Battle battle) {
        String battleId = String.valueOf(100000 + new java.util.Random().nextInt(900000));

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("battles")
                .child(battleId);

        ref.setValueAsync(battle);
        return battleId;
    }

    public void joinBattle(String battleId, Player player2) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("battles")
                .child(battleId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Battle battle = snapshot.getValue(Battle.class);
                if (battle != null && "WAITING".equals(battle.status)) {
                    battle.player2 = player2;
                    battle.status = "FINISHED";

                    // Логика определения победителя
                    if (battle.player1.score > player2.score) {
                        battle.winnerId = battle.player1.userId;
                    } else if (battle.player1.score < player2.score) {
                        battle.winnerId = player2.userId;
                    } else {
                        battle.winnerId = 0L; // Ничья
                    }

                    // Сохраняем обновленные данные дуэли в Firebase
                    ref.setValueAsync(battle);

                    // Сохраняем финальный результат дуэли в MySQL!
                    BattleHistory history = new BattleHistory(
                            battle.player1.userId, battle.player1.username, battle.player1.score,
                            battle.player2.userId, battle.player2.username, battle.player2.score,
                            battle.winnerId
                    );
                    battleHistoryRepository.save(history);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Ошибка чтения данных из Firebase: " + error.getMessage());
            }
        });
    }
}
