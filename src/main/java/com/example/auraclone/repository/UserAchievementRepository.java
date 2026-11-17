package com.example.auraclone.repository;

import com.example.auraclone.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUserIdOrderByUnlockedAtAsc(Long userId);
    boolean existsByUserIdAndCode(Long userId, String code);

    @Modifying
    void deleteByUserId(Long userId);
}
