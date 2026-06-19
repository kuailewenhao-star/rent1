package com.rent1.infrastructure.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 内存缓存实现（Redis 不可用时的降级方案）
 * 当配置 spring.cache.type=none 或 Redis 连接失败时启用
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "none", matchIfMissing = false)
public class InMemoryCacheService implements CacheService {

    /**
     * 缓存数据：key -> [value, expireTimeMs]
     * expireTimeMs = 0 表示永不过期
     */
    private final Map<String, Object[]> cache = new ConcurrentHashMap<>();

    /**
     * 锁存储：lockKey -> expireTimeMs
     */
    private final Map<String, Long> locks = new ConcurrentHashMap<>();

    @Override
    public void set(String key, String value, long timeout, TimeUnit unit) {
        long expireAt = timeout > 0 ? System.currentTimeMillis() + unit.toMillis(timeout) : 0L;
        cache.put(key, new Object[]{value, expireAt});
    }

    @Override
    public String get(String key) {
        Object[] entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        long expireAt = (Long) entry[1];
        if (expireAt > 0 && System.currentTimeMillis() > expireAt) {
            cache.remove(key);
            return null;
        }
        return (String) entry[0];
    }

    @Override
    public Boolean delete(String key) {
        cache.remove(key);
        return true;
    }

    @Override
    public Boolean acquireLock(String lockKey, long timeout, TimeUnit unit) {
        long now = System.currentTimeMillis();
        long expireAt = timeout > 0 ? now + unit.toMillis(timeout) : now + 60000L;

        Long existing = locks.putIfAbsent(lockKey, expireAt);
        if (existing == null) {
            return true;
        }
        // 已过期则覆盖
        if (existing > 0 && now > existing) {
            locks.put(lockKey, expireAt);
            return true;
        }
        return false;
    }

    @Override
    public void releaseLock(String lockKey) {
        locks.remove(lockKey);
    }

    @Override
    public Boolean exists(String key) {
        return get(key) != null;
    }

    @Override
    public Boolean nonceExists(String nonce) {
        return exists("nonce:" + nonce);
    }

    @Override
    public void storeNonce(String nonce, long expireSeconds) {
        set("nonce:" + nonce, "1", expireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public String getImplementationName() {
        return "InMemory";
    }
}
