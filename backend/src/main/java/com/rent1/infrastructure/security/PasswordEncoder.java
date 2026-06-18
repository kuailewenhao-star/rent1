package com.rent1.infrastructure.security;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码加密服务
 * 使用 SHA-256 + salt 进行密码哈希
 */
@Service
public class PasswordEncoder {

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 密码加密（SHA-256 + salt）
     */
    public String encode(String rawPassword) {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        String saltStr = Base64.getEncoder().encodeToString(salt);
        String hashed = DigestUtils.sha256Hex(rawPassword + saltStr);
        return "$sha256$" + saltStr + "$" + hashed;
    }

    /**
     * 密码校验
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || !encodedPassword.startsWith("$sha256$")) {
            return false;
        }
        String[] parts = encodedPassword.split("\\$", -1);
        // Format: "", "sha256", "salt", "hash", ""
        if (parts.length < 4) {
            return false;
        }
        String salt = parts[2];
        String expectedHash = parts[3];
        String computedHash = DigestUtils.sha256Hex(rawPassword + salt);
        return computedHash.equals(expectedHash);
    }
}
