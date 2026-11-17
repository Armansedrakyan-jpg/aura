package com.example.auraclone.service;

import com.example.auraclone.dto.Battle;
import com.example.auraclone.dto.Player;
import com.example.auraclone.entity.AuraAnalysis;
import com.example.auraclone.entity.BattleHistory;
import com.example.auraclone.entity.User;
import com.example.auraclone.exception.ApiException;
import com.example.auraclone.repository.AuraAnalysisRepository;
import com.example.auraclone.repository.BattleHistoryRepository;
import com.example.auraclone.repository.UserRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class BattleService {

    private static final long FIREBASE_TIMEOUT_SECONDS = 10L;
    private static final int MAX_PIN_ATTEMPTS = 5;
    private final SecureRandom random = new SecureRandom();

    private final BattleHistoryRepository battleHistoryRepository;
    private final UserRepository userRepository;
    private final AuraAnalysisRepository auraAnalysisRepository;
    private final AchievementService achievementService;

    public BattleService(BattleHistoryRepository battleHistoryRepository,
                         UserRepository userRepository,
                         AuraAnalysisRepository auraAnalysisRepository,
                         AchievementService achievementService) {
        this.battleHistoryRepository = battleHistoryRepository;
        this.userRepository = userRepository;
        this.auraAnalysisRepository = auraAnalysisRepository;
        this.achievementService = achievementService;
    }

    public String startBattle(Long userId, Player requestedPlayer) {
        ensureFirebaseInitialized();
        Player verifiedPlayer = buildVerifiedPlayer(userId, requestedPlayer);
        Battle battle = new Battle("WAITING", System.currentTimeMillis(), verifiedPlayer);

        for (int attempt = 0; attempt < MAX_PIN_ATTEMPTS; attempt++) {
            String battleId = String.valueOf(100000 + random.nextInt(900000));
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("battles")
                    .child(battleId);

            if (tryCreateRoom(ref, battle)) {
                return battleId;
            }
        }
        throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Не удалось создать комнату, попробуйте ещё раз");
    }

    public Battle joinBattle(String battleId, Long userId, Player requestedPlayer) {
        if (battleId == null || !battleId.matches("^\\d{6}$")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Некорректный код комнаты");
        }
        ensureFirebaseInitialized();
        Player player2 = buildVerifiedPlayer(userId, requestedPlayer);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("battles")
                .child(battleId);

        Battle finishedBattle = executeJoinTransaction(ref, player2);

        BattleHistory history = new BattleHistory(
                finishedBattle.player1.userId, finishedBattle.player1.username, finishedBattle.player1.score,
                finishedBattle.player2.userId, finishedBattle.player2.username, finishedBattle.player2.score,
                finishedBattle.winnerId
        );
        battleHistoryRepository.save(history);
        achievementService.evaluateBattleAchievements(finishedBattle.winnerId);

        return finishedBattle;
    }

    public List<BattleHistory> getUserBattleHistory(Long userId) {
        return battleHistoryRepository.findUserBattles(userId, PageRequest.of(0, 50));
    }

    Player buildVerifiedPlayer(Long userId, Player requestedPlayer) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        AuraAnalysis latestAnalysis = auraAnalysisRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElse(null);

        if (latestAnalysis != null) {
            return new Player(user.getId(), user.getUsername(), latestAnalysis.getScore(), latestAnalysis.getLevel());
        }

        if (requestedPlayer != null) {
            int safeScore = Math.max(0, Math.min(100, requestedPlayer.score));
            String safeTitle = requestedPlayer.title == null || requestedPlayer.title.isBlank()
                    ? "MID"
                    : requestedPlayer.title.trim();
            return new Player(user.getId(), user.getUsername(), safeScore, safeTitle);
        }

        throw new ApiException(HttpStatus.BAD_REQUEST, "Сначала пройди анализ ауры перед дуэлью");
    }

    static Long resolveWinner(Player player1, Player player2) {
        if (player1.score > player2.score) {
            return player1.userId;
        }
        if (player1.score < player2.score) {
            return player2.userId;
        }
        return 0L;
    }

    private void ensureFirebaseInitialized() {
        if (FirebaseApp.getApps().isEmpty()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Сервис дуэлей временно недоступен");
        }
    }

    private boolean tryCreateRoom(DatabaseReference ref, Battle battle) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        ref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue() != null) {
                    return Transaction.abort();
                }
                currentData.setValue(battle);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (error != null) {
                    future.completeExceptionally(new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Ошибка Firebase"));
                } else {
                    future.complete(committed);
                }
            }
        });
        return awaitFuture(future);
    }

    private Battle executeJoinTransaction(DatabaseReference ref, Player player2) {
        CompletableFuture<Battle> future = new CompletableFuture<>();
        AtomicReference<ApiException> failureReason = new AtomicReference<>();

        ref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                Battle battle = currentData.getValue(Battle.class);
                if (battle == null) {
                    failureReason.set(new ApiException(HttpStatus.NOT_FOUND, "Комната не найдена"));
                    return Transaction.abort();
                }
                if (!"WAITING".equals(battle.status) || battle.player2 != null) {
                    failureReason.set(new ApiException(HttpStatus.CONFLICT, "Дуэль уже началась или завершена"));
                    return Transaction.abort();
                }
                if (battle.player1 != null && player2.userId.equals(battle.player1.userId)) {
                    failureReason.set(new ApiException(HttpStatus.BAD_REQUEST, "Нельзя присоединиться к собственной дуэли"));
                    return Transaction.abort();
                }

                battle.player2 = player2;
                battle.status = "FINISHED";
                battle.winnerId = resolveWinner(battle.player1, player2);
                currentData.setValue(battle);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (error != null) {
                    future.completeExceptionally(new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Ошибка Firebase"));
                    return;
                }
                if (!committed) {
                    ApiException reason = failureReason.get();
                    future.completeExceptionally(reason != null
                            ? reason
                            : new ApiException(HttpStatus.CONFLICT, "Не удалось присоединиться к дуэли"));
                    return;
                }
                Battle updated = currentData.getValue(Battle.class);
                future.complete(updated);
            }
        });

        return awaitFuture(future);
    }

    private <T> T awaitFuture(CompletableFuture<T> future) {
        try {
            return future.get(FIREBASE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new ApiException(HttpStatus.GATEWAY_TIMEOUT, "Превышено время ожидания Firebase");
        } catch (Exception e) {
            if (e.getCause() instanceof ApiException apiEx) {
                throw apiEx;
            }
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Ошибка синхронизации дуэли");
        }
    }
}

