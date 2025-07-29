package org.example.authdemo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.example.authdemo.model.Role;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;

@Component
public class JwtTokenProvider {

    private final JwtKeyProvider keyProvider;
    private final long accessTokenExpireTimeMs = 1 * 60 * 1000;

    public JwtTokenProvider(JwtKeyProvider keyProvider) throws Exception {
        this.keyProvider = keyProvider;
    }

    public String generateAccessToken(String login, Set<Role> roles) {
        return Jwts.builder()
                .setSubject(login)
                .claim("roles", roles.stream().map(Enum::name).toList())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpireTimeMs))
                .signWith(keyProvider.getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(keyProvider.getPublicKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getLoginFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(keyProvider.getPublicKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
