package com.rent1.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * AES加解密服务（基础设施层 - 无业务逻辑）
 * <p>
 * 提供两种加密模式：
 * <ul>
 *     <li>{@link Mode#RANDOM_IV}：每次加密使用随机 IV，同明文得到不同密文。
 *         用于传输加密（请求体加密、Token 等），安全性最高。</li>
 *     <li>{@link Mode#STABLE}：使用密钥派生的固定 IV，同明文在同一密钥下
 *         得到相同密文。用于数据库敏感字段加密，支持 "WHERE 字段 = ?" 的
 *         等值查询。</li>
 * </ul>
 * 密文格式版本：{@code ENC$V1$base64(iv+cipher)} 或 {@code ENC$S$base64(cipher)}
 * </p>
 * 对应任务：SEC-001 敏感字段数据库加密存储；SEC-002 传输加密
 */
@Slf4j
@Service
public class CryptoService {

    public enum Mode {
        /** 随机 IV - 用于传输加密 */
        RANDOM_IV,
        /** 稳定 IV - 用于数据库加密，支持等值查询 */
        STABLE
    }

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    /** AES-256 需要 32 字节密钥 */
    private static final int KEY_LENGTH = 32;
    /** CBC 模式 IV 固定为 16 字节 */
    private static final int IV_LENGTH = 16;

    /** 随机 IV 版本前缀（RANDOM_IV） */
    private static final String VERSION_PREFIX_RANDOM = "ENC$V1$";
    /** 稳定 IV 版本前缀（STABLE） */
    private static final String VERSION_PREFIX_STABLE = "ENC$S$";

    @Value("${crypto.aes.secret-key}")
    private String secretKey;

    private byte[] keyBytes;
    /** 稳定模式使用的 IV（派生自 secret key） */
    private byte[] stableIvBytes;

    @PostConstruct
    public void init() {
        keyBytes = padKey(secretKey).getBytes(StandardCharsets.UTF_8);
        // 通过 SHA-256 派生稳定 IV，使 IV 与 key 强绑定：
        // 1. key 不变则 IV 不变；
        // 2. 不同 key 对应不同 IV。
        stableIvBytes = deriveStableIv(keyBytes);
    }

    /**
     * 使用稳定 IV 加密（同明文 → 同密文）
     * 用于数据库字段，支持等值查询。
     */
    public String encryptStable(String plainText) {
        return encrypt(plainText, Mode.STABLE);
    }

    /**
     * 使用随机 IV 加密（同明文 → 不同密文）
     * 用于传输、Token 等场景，安全性更高。
     */
    public String encrypt(String plainText) {
        return encrypt(plainText, Mode.RANDOM_IV);
    }

    public String encrypt(String plainText, Mode mode) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            byte[] iv = (mode == Mode.STABLE) ? stableIvBytes : generateIv();
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            String prefix = (mode == Mode.STABLE) ? VERSION_PREFIX_STABLE : VERSION_PREFIX_RANDOM;
            if (mode == Mode.STABLE) {
                // 稳定 IV 无需将 IV 记录在密文中，因为解密时从 key 重新派生即可
                return prefix + Base64.encodeBase64String(cipherText);
            }
            byte[] combined = new byte[IV_LENGTH + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(cipherText, 0, combined, IV_LENGTH, cipherText.length);
            return prefix + Base64.encodeBase64String(combined);
        } catch (Exception e) {
            log.error("AES encrypt failed", e);
            throw new IllegalStateException("敏感字段加密失败", e);
        }
    }

    /**
     * 解密。根据密文前缀识别加密模式并正确解密。
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        try {
            final byte[] raw;
            final byte[] iv;
            if (encryptedText.startsWith(VERSION_PREFIX_STABLE)) {
                String payload = encryptedText.substring(VERSION_PREFIX_STABLE.length());
                raw = Base64.decodeBase64(payload);
                iv = stableIvBytes;
            } else if (encryptedText.startsWith(VERSION_PREFIX_RANDOM)) {
                String payload = encryptedText.substring(VERSION_PREFIX_RANDOM.length());
                byte[] combined = Base64.decodeBase64(payload);
                if (combined.length <= IV_LENGTH) {
                    throw new IllegalStateException("非法密文格式");
                }
                iv = new byte[IV_LENGTH];
                raw = new byte[combined.length - IV_LENGTH];
                System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
                System.arraycopy(combined, IV_LENGTH, raw, 0, raw.length);
            } else {
                // 向后兼容：未识别的前缀当作纯 Base64 密文（旧格式 / 明文）
                try {
                    byte[] combined = Base64.decodeBase64(encryptedText);
                    if (combined.length <= IV_LENGTH) {
                        return encryptedText;
                    }
                    iv = new byte[IV_LENGTH];
                    raw = new byte[combined.length - IV_LENGTH];
                    System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
                    System.arraycopy(combined, IV_LENGTH, raw, 0, raw.length);
                } catch (Exception ex) {
                    return encryptedText;
                }
            }

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            return new String(cipher.doFinal(raw), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES decrypt failed", e);
            throw new IllegalStateException("敏感字段解密失败", e);
        }
    }

    /**
     * 判定是否看起来是我们生成的密文。
     */
    public boolean isEncrypted(String text) {
        return text != null
                && (text.startsWith(VERSION_PREFIX_RANDOM) || text.startsWith(VERSION_PREFIX_STABLE));
    }

    /**
     * 对稳定加密模式做外部公开暴露（等值查询使用）
     *
     * @deprecated 统一使用 {@link #encryptStable(String)}
     */
    @Deprecated
    public String stableEncrypt(String plainText) {
        return encryptStable(plainText);
    }

    private byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private byte[] deriveStableIv(byte[] key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(key);
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(digest, 0, iv, 0, IV_LENGTH);
            return iv;
        } catch (Exception e) {
            // fallback：直接使用 key 的前 16 字节
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(key, 0, iv, 0, Math.min(key.length, IV_LENGTH));
            return iv;
        }
    }

    private String padKey(String key) {
        if (key == null) {
            key = "";
        }
        if (key.length() < KEY_LENGTH) {
            StringBuilder sb = new StringBuilder(key);
            while (sb.length() < KEY_LENGTH) {
                sb.append('0');
            }
            return sb.toString();
        }
        return key.substring(0, KEY_LENGTH);
    }
}
