    package org.example.authdemo.dto;

    import lombok.AllArgsConstructor;
    import lombok.Data;

    @Data
    @AllArgsConstructor
    public class TokenResponse {
        private String accessToken;
        private String refreshToken;
    }
