package com.example.auraclone.service;

import com.example.auraclone.dto.CooldownStatusResponse;
import com.example.auraclone.entity.User;
import com.example.auraclone.exception.ApiException;
import com.example.auraclone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuraQuotaService {

    public enum ReservationType {
        FREE,
        CREDIT
    }

    public record QuotaReservation(ReservationType type, LocalDateTime previousLastFreeAt) {}

    private final UserRepository userRepository;
    private final long cooldownHours;

    public AuraQuotaService(UserRepository userRepository,
                            @Value("${app.aura.cooldown-hours:12}") long cooldownHours) {
        this.userRepository = userRepository;
        this.cooldownHours = Math.max(0L, cooldownHours);
    }

    @Transactional
    public QuotaReservation reserveForAnalysis(Long userId) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        LocalDateTime now = LocalDateTime.now();
        CooldownStatusResponse status = computeStatus(user, now);

        if (status.canGenerateFree) {
            LocalDateTime previous = user.getLastFreeAnalysisAt();
            user.setLastFreeAnalysisAt(now);
            userRepository.save(user);
            return new QuotaReservation(ReservationType.FREE, previous);
        }

        if (user.getCredits() > 0) {
            user.setCredits(user.getCredits() - 1);
            userRepository.save(user);
            return new QuotaReservation(ReservationType.CREDIT, null);
        }

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("remainingSeconds", status.remainingSeconds);
        details.put("nextFreeAt", status.nextFreeAt);
        details.put("credits", status.credits);

        throw new ApiException(
                HttpStatus.TOO_MANY_REQUESTS,
                buildCooldownMessage(status.remainingSeconds),
                details
        );
    }

    @Transactional
    public void rollbackReservation(Long userId, QuotaReservation reservation) {
        if (reservation == null) {
            return;
        }
        userRepository.findByIdForUpdate(userId).ifPresent(user -> {
            if (reservation.type() == ReservationType.FREE) {
                user.setLastFreeAnalysisAt(reservation.previousLastFreeAt());
            } else if (reservation.type() == ReservationType.CREDIT) {
                user.setCredits(user.getCredits() + 1);
            }
            userRepository.save(user);
        });
    }

    public CooldownStatusResponse getCooldownStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        return computeStatus(user, LocalDateTime.now());
    }

    private CooldownStatusResponse computeStatus(User user, LocalDateTime now) {
        if (cooldownHours == 0L || user.getLastFreeAnalysisAt() == null) {
            return new CooldownStatusResponse(true, 0L, null, user.getCredits());
        }

        LocalDateTime nextFreeAt = user.getLastFreeAnalysisAt().plusHours(cooldownHours);
        if (!now.isBefore(nextFreeAt)) {
            return new CooldownStatusResponse(true, 0L, null, user.getCredits());
        }

        long remainingSeconds = Math.max(1L, Duration.between(now, nextFreeAt).getSeconds());
        return new CooldownStatusResponse(false, remainingSeconds, nextFreeAt, user.getCredits());
    }

    static String buildCooldownMessage(long remainingSeconds) {
        long hours = remainingSeconds / 3600;
        long minutes = (remainingSeconds % 3600) / 60;
        long seconds = remainingSeconds % 60;

        if (hours > 0 && minutes > 0) {
            return String.format("Следующая бесплатная генерация будет доступна через %d ч %d мин", hours, minutes);
        }
        if (hours > 0) {
            return String.format("Следующая бесплатная генерация будет доступна через %d ч", hours);
        }
        if (minutes > 0) {
            return String.format("Следующая бесплатная генерация будет доступна через %d мин", minutes);
        }
        return String.format("Следующая бесплатная генерация будет доступна через %d сек", Math.max(1L, seconds));
    }
}
