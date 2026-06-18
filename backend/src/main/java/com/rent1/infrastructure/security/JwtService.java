package com.rent1.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token服务
 */
@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration.user}")
    private long userExpiration;

    @Value("${jwt.expiration.admin}")
    private long adminExpiration;

    /**
     * 生成用户Token（24小时有效）
     */
    public String generateUserToken(String memberId, String memberType, String subjectId) {
        return generateToken(memberId, memberType, subjectId, userExpiration);
    }

    /**
     * 生成管理员Token（8小时有效）
     */
    public String generateAdminToken(String adminId, String permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("adminId", adminId);
        claims.put("permissions", permissions);
        claims.put("memberType", "ADMIN");
        return createToken(claims, adminId, adminExpiration);
    }

    /**
     * 验证Token
     */
    public Claims parseToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.warn("JWT token invalid: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String generateToken(String memberId, String memberType, String subjectId, long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("memberId", memberId);
        claims.put("memberType", memberType);
        claims.put("subjectId", subjectId);
        return createToken(claims, memberId, expiration);
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
