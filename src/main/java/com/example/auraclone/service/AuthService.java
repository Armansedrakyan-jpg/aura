package com.example.auraclone.service;

import com.example.auraclone.dto.AuthDtos.*;
import com.example.auraclone.entity.User;
import com.example.auraclone.exception.ApiException;
import com.example.auraclone.repository.AuraAnalysisRepository;
import com.example.auraclone.repository.BattleHistoryRepository;
import com.example.auraclone.repository.PurchaseTransactionRepository;
import com.example.auraclone.repository.UserAchievementRepository;
import com.example.auraclone.repository.UserInventoryRepository;
import com.example.auraclone.repository.UserRepository;
import com.example.auraclone.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuraAnalysisRepository auraAnalysisRepository;
    private final BattleHistoryRepository battleHistoryRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final PurchaseTransactionRepository purchaseTransactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       AuraAnalysisRepository auraAnalysisRepository,
                       BattleHistoryRepository battleHistoryRepository,
                       UserAchievementRepository userAchievementRepository,
                       UserInventoryRepository userInventoryRepository,
                       PurchaseTransactionRepository purchaseTransactionRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.auraAnalysisRepository = auraAnalysisRepository;
        this.battleHistoryRepository = battleHistoryRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.userInventoryRepository = userInventoryRepository;
        this.purchaseTransactionRepository = purchaseTransactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        String username = request.username.trim();
        String email = request.email.trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "Username уже занят");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email уже используется");
        }

        User user = new User(
                username,
                email,
                passwordEncoder.encode(request.password)
        );
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, user.getId(), user.getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.usernameOrEmail.trim();

        User user = userRepository.findByUsernameIgnoreCase(identifier)
                .or(() -> userRepository.findByEmailIgnoreCase(identifier.toLowerCase(Locale.ROOT)))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль"));

        if (!passwordEncoder.matches(request.password, user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, user.getId(), user.getUsername());
    }

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        return new UserProfileResponse(user.getId(), user.getUsername(), user.getEmail());
    }

    @Transactional
    public void deleteAccount(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }
        auraAnalysisRepository.deleteByUserId(userId);
        userAchievementRepository.deleteByUserId(userId);
        userInventoryRepository.deleteByUserId(userId);
        purchaseTransactionRepository.detachUser(userId);
        battleHistoryRepository.anonymizePlayer1(userId);
        battleHistoryRepository.anonymizePlayer2(userId);
        userRepository.deleteById(userId);
    }
}

