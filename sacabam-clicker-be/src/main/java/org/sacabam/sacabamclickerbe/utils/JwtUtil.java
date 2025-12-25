package org.sacabam.sacabamclickerbe.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${jwt.secret:sacabam-clicker-secret-key-for-jwt-token-generation}")
    private String jwtSecret;

    @Value("${jwt.expiration:900}") // 15 minutes default
    private Integer jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String email, Integer userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration * 1000L);

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateTokenWithPermissions(String email, Integer userId, List<String> permissions) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration * 1000L);

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("permissions", permissions)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public Integer getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", Integer.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("permissions", List.class);
    }

    public boolean validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                System.out.println("🔍 JWT Debug: Token is null or empty");
                return false;
            }

            System.out.println("🔍 JWT Debug: Validating token: " + token.substring(0, Math.min(20, token.length())) + "...");

            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            System.out.println("✅ JWT Debug: Token validation successful");
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("❌ JWT Debug: Token expired - " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            System.out.println("❌ JWT Debug: Malformed token - " + e.getMessage());
            return false;
        } catch (SignatureException e) {
            System.out.println("❌ JWT Debug: Invalid signature - " + e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("❌ JWT Debug: Token validation failed - " + e.getMessage());
            return false;
        }
    }

    public Integer getExpirationTime() {
        return jwtExpiration;
    }
}