package com.example.auraclone.repository;

import com.example.auraclone.entity.UserInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserInventoryRepository extends JpaRepository<UserInventory, Long> {
    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    @Query("SELECT ui.itemId FROM UserInventory ui WHERE ui.userId = :userId")
    List<Long> findItemIdsByUserId(@Param("userId") Long userId);

    @Modifying
    void deleteByUserId(Long userId);
}
