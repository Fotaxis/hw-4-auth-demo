package org.example.authdemo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.authdemo.dto.LoginRequest;
import org.example.authdemo.dto.RefreshTokenRequest;
import org.example.authdemo.dto.RegisterRequest;
import org.example.authdemo.dto.TokenResponse;
import org.example.authdemo.exception.CustomException;
import org.example.authdemo.model.RefreshToken;
import org.example.authdemo.model.User;
import org.example.authdemo.service.TokenService;
import org.example.authdemo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final TokenService tokenService;

    public AuthController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterRequest req) {
        userService.register(req);
        return ResponseEntity.ok("User registered");

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        User user = userService.findByLogin(req.getLogin());
        if (!userService.checkPassword(req.getPassword(), user.getPassword())) {
            throw new CustomException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
        String ip = extractIp(request);

        String accessToken = tokenService.generateAccessToken(user);
        RefreshToken refreshToken = tokenService.createRefreshToken(user, ip);

        return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request,
                                               HttpServletRequest httpRequest) {
        String requestToken = request.getRefreshToken();

        RefreshToken refreshToken = tokenService.findByToken(requestToken)
                .orElseThrow(() -> new CustomException("Refresh token not found", HttpStatus.BAD_REQUEST));

        tokenService.verifyExpiration(refreshToken);

        String currentIp = extractIp(httpRequest);
        if (!refreshToken.getIpAddress().equals(currentIp)) {
            throw new CustomException("Refresh token used from new IP - Please login again", HttpStatus.UNAUTHORIZED);
        }

        User user = refreshToken.getUser();

        RefreshToken newRefreshToken = tokenService.createRefreshToken(user, currentIp);
        String newAccessToken = tokenService.generateAccessToken(user);

        return ResponseEntity.ok(new TokenResponse(newAccessToken, newRefreshToken.getToken()));
    }

    @PostMapping("/revoke")
    public ResponseEntity<?> revokeToken(@Valid @RequestBody RefreshTokenRequest request) {
        String requestToken = request.getRefreshToken();

        RefreshToken refreshToken = tokenService.findByToken(requestToken)
                .orElseThrow(() -> new CustomException("Token not found", HttpStatus.BAD_REQUEST));

        tokenService.deleteByUser(refreshToken.getUser());

        return ResponseEntity.ok("Refresh token revoked");
    }

    private String extractIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        return (ip == null || ip.isEmpty()) ? request.getRemoteAddr() : ip;
    }

}

