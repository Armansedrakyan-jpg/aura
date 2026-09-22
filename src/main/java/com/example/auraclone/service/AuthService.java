package com.example.auraclone.service;

import com.example.auraclone.dto.AuthDtos.*;
import com.example.auraclone.entity.User;
import com.example.auraclone.repository.UserRepository;
import com.example.auraclone.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username)) {
            throw new IllegalArgumentException("Username уже занят");
        }
        if (userRepository.existsByEmail(request.email)) {
            throw new IllegalArgumentException("Email уже используется");
        }

        User user = new User(
                request.username,
                request.email,
                passwordEncoder.encode(request.password)
        );
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, user.getId(), user.getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.usernameOrEmail)
                .or(() -> userRepository.findByEmail(request.usernameOrEmail))
                .orElseThrow(() -> new IllegalArgumentException("Неверный логин или пароль"));

        if (!passwordEncoder.matches(request.password, user.getPassword())) {
            throw new IllegalArgumentException("Неверный логин или пароль");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, user.getId(), user.getUsername());
    }
}
