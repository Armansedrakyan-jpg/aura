package com.example.auraclone.dto;

public class AuthDtos {

    public static class RegisterRequest {
        public String username;
        public String email;
        public String password;
    }

    public static class LoginRequest {
        public String usernameOrEmail;
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
}
