package com.example.auraclone.service;

import com.example.auraclone.dto.Player;
import com.example.auraclone.entity.AuraAnalysis;
import com.example.auraclone.entity.User;
import com.example.auraclone.exception.ApiException;
import com.example.auraclone.repository.AuraAnalysisRepository;
import com.example.auraclone.repository.BattleHistoryRepository;
import com.example.auraclone.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BattleServiceTest {

    @Mock
    private BattleHistoryRepository battleHistoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuraAnalysisRepository auraAnalysisRepository;
    @Mock
    private AchievementService achievementService;

    @InjectMocks
    private BattleService battleService;

    @Test
    void buildVerifiedPlayerPrefersServerSideScoreOverClientPayload() {
        User user = new User("arman", "arman@example.com", "hash");
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(auraAnalysisRepository.findTopByUserIdOrderByCreatedAtDesc(10L))
                .thenReturn(Optional.of(new AuraAnalysis(10L, 77, "Сигма", "", "", "", "", "")));

        Player cheatAttempt = new Player(999L, "hacker", 99999, "Cheater");
        Player verified = battleService.buildVerifiedPlayer(10L, cheatAttempt);

        assertEquals("arman", verified.username);
        assertEquals(77, verified.score);
        assertEquals("Сигма", verified.title);
    }

    @Test
    void resolveWinnerHandlesWinLossAndDraw() {
        Player p1 = new Player(1L, "a", 80, "A");
        Player p2 = new Player(2L, "b", 60, "B");
        Player p3 = new Player(3L, "c", 80, "C");

        assertEquals(1L, BattleService.resolveWinner(p1, p2));
        assertEquals(1L, BattleService.resolveWinner(p2, p1));
        assertEquals(0L, BattleService.resolveWinner(p1, p3));
    }

    @Test
    void joinBattleRejectsInvalidPinFormat() {
        ApiException ex = assertThrows(ApiException.class,
                () -> battleService.joinBattle("abc", 1L, new Player()));
        assertEquals(400, ex.getStatus().value());
    }
}
