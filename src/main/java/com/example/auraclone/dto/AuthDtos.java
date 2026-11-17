package com.example.auraclone.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public static class RegisterRequest {
        @NotBlank(message = "Username обязателен")
        @Size(min = 3, max = 30, message = "Username должен быть от 3 до 30 символов")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username может содержать только латинские буквы, цифры и . _ -")
        public String username;

        @NotBlank(message = "Email обязателен")
        @Email(message = "Некорректный формат email")
        @Size(max = 120, message = "Email слишком длинный")
        public String email;

        @NotBlank(message = "Пароль обязателен")
        @Size(min = 6, max = 100, message = "Пароль должен быть от 6 до 100 символов")
        public String password;
    }

    public static class LoginRequest {
        @NotBlank(message = "Логин или email обязателен")
        public String usernameOrEmail;

        @NotBlank(message = "Пароль обязателен")
        public String password;
    }

    public static class AuthResponse {
        public String token;
        public Long userId;
        public String username;

        public AuthResponse(String token, Long userId, String username) {
            this.token = token;
            this.userId = userId;
            this.username = username;
        }
    }

    public static class UserProfileResponse {
        public Long userId;
        public String username;
        public String email;

        public UserProfileResponse(Long userId, String username, String email) {
            this.userId = userId;
            this.username = username;
            this.email = email;
        }
    }
}

