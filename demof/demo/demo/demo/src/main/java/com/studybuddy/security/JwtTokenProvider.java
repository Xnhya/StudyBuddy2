package com.studybuddy.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret:mySecretKeyForJwtTokenGeneration12345}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}") // 24 horas por defecto
    private int jwtExpirationInMs;

    // Generar token (nueva API JJWT)
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(username)  // Nuevo método: .subject() en lugar de .setSubject()
                .issuedAt(now)      // Nuevo método: .issuedAt() en lugar de .setIssuedAt()
                .expiration(expiryDate) // Nuevo método: .expiration() en lugar de .setExpiration()
                .signWith(getSigningKey())
                .compact();
    }

    // Obtener username del token (nueva API JJWT)
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // Nuevo método: .verifyWith() en lugar de .setSigningKey()
                .build()
                .parseSignedClaims(token)     // Nuevo método: .parseSignedClaims() en lugar de .parseClaimsJws()
                .getPayload()
                .getSubject();                // El subject sigue siendo igual
    }

    // Validar token (nueva API JJWT)
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException ex) {
            logger.error("Error validando token JWT: {}", ex.getMessage());
            return false;
        }
    }

    private SecretKey getSigningKey() {
        // Asegurarse de que la clave tenga al menos 256 bits (32 caracteres)
        byte[] keyBytes;
        if (jwtSecret.length() < 32) {
            // Si es más corta, repetir o rellenar
            String paddedSecret = String.format("%-32s", jwtSecret).replace(' ', '0');
            keyBytes = paddedSecret.getBytes(StandardCharsets.UTF_8);
        } else {
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Métodos adicionales útiles (actualizados)
    public Date getExpirationDateFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }

    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
}