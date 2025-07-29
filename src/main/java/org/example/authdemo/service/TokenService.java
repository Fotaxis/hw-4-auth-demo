package org.example.authdemo.service;

import jakarta.transaction.Transactional;
import org.example.authdemo.exception.CustomException;
import org.example.authdemo.model.RefreshToken;
import org.example.authdemo.model.User;
import org.example.authdemo.repository.RefreshTokenRepository;
import org.example.authdemo.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final long refreshTokenExpiteTime = 7 * 24 * 60 * 60 * 1000L;

    public TokenService(RefreshTokenRepository refreshTokenRepository, JwtTokenProvider jwtTokenProvider) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenExpiteTime));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void verifyExpiration(RefreshToken token) {
        if(token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new CustomException("Refresh token expired. Please login again.", HttpStatus.UNAUTHORIZED);
        }
    }

    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    public String generateAccessToken(User user) {
        return jwtTokenProvider.generateAccessToken(user.getLogin(), user.getRoles());
    }
}
