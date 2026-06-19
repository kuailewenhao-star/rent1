package com.rent1.infrastructure.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存服务（默认实现）
 * 如果运行时 Redis 连接失败，会自动降级为内存缓存（In-Memory）
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
public class RedisService implements CacheService {

    private StringRedisTemplate redisTemplate;

    /**
     * 运行时降级开关：true 表示使用内存缓存
     */
    private volatile boolean useMemoryFallback = false;

    /**
     * 内存降级缓存
     */
    private final Map<String, Object[]> memoryCache = new ConcurrentHashMap<>();
    private final Map<String, Long> memoryLocks = new ConcurrentHashMap<>();

    @Autowired(required = false)
    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        if (redisTemplate == null) {
            log.warn("[CacheService] StringRedisTemplate 未注入，使用内存缓存");
            useMemoryFallback = true;
            return;
        }
        // 启动时探测 Redis 可用性：写入一个探测 key（2秒过期）
        try {
            redisTemplate.opsForValue().set("rent1:probe:" + System.currentTimeMillis(), "1", 2, TimeUnit.SECONDS);
            log.info("[CacheService] Redis 连接正常，使用 Redis 缓存");
        } catch (Exception e) {
            log.warn("[CacheService] Redis 连接失败({})，自动降级为内存缓存", e.getMessage());
            useMemoryFallback = true;
        }
    }

    // ==================== 基础接口 ====================

    @Override
    public void set(String key, String value, long timeout, TimeUnit unit) {
        if (useMemoryFallback) {
            long expireAt = timeout > 0 ? System.currentTimeMillis() + unit.toMillis(timeout) : 0L;
            memoryCache.put(key, new Object[]{value, expireAt});
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (RedisConnectionFailureException e) {
            triggerFallback("set", e);
            memoryCache.put(key, new Object[]{value, unit.toMillis(timeout)});
        } catch (Exception e) {
            log.error("[CacheService] Redis set failed, key: {}, falling back", key, e);
            long expireAt = timeout > 0 ? System.currentTimeMillis() + unit.toMillis(timeout) : 0L;
            memoryCache.put(key, new Object[]{value, expireAt});
        }
    }

    @Override
    public String get(String key) {
        if (useMemoryFallback) {
            return memoryGet(key);
        }
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (RedisConnectionFailureException e) {
            triggerFallback("get", e);
            return memoryGet(key);
        } catch (Exception e) {
            log.error("[CacheService] Redis get failed, key: {}, falling back", key, e);
            return memoryGet(key);
        }
    }

    @Override
    public Boolean delete(String key) {
        if (useMemoryFallback) {
            memoryCache.remove(key);
            return true;
        }
        try {
            return redisTemplate.delete(key);
        } catch (RedisConnectionFailureException e) {
            triggerFallback("delete", e);
            memoryCache.remove(key);
            return true;
        } catch (Exception e) {
            log.error("[CacheService] Redis delete failed, key: {}, falling back", key, e);
            memoryCache.remove(key);
            return false;
        }
    }

    @Override
    public Boolean acquireLock(String lockKey, long timeout, TimeUnit unit) {
        if (useMemoryFallback) {
            return memoryAcquireLock(lockKey, timeout, unit);
        }
        try {
            Boolean result = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "LOCKED", timeout, unit);
            return Boolean.TRUE.equals(result);
        } catch (RedisConnectionFailureException e) {
            triggerFallback("acquireLock", e);
            return memoryAcquireLock(lockKey, timeout, unit);
        } catch (Exception e) {
            log.error("[CacheService] Redis acquire lock failed, key: {}, falling back", lockKey, e);
            return memoryAcquireLock(lockKey, timeout, unit);
        }
    }

    @Override
    public void releaseLock(String lockKey) {
        if (useMemoryFallback) {
            memoryLocks.remove(lockKey);
            return;
        }
        try {
            redisTemplate.delete(lockKey);
        } catch (RedisConnectionFailureException e) {
            triggerFallback("releaseLock", e);
            memoryLocks.remove(lockKey);
        } catch (Exception e) {
            log.error("[CacheService] Redis release lock failed, key: {}, falling back", lockKey, e);
            memoryLocks.remove(lockKey);
        }
    }

    @Override
    public Boolean exists(String key) {
        if (useMemoryFallback) {
            return memoryGet(key) != null;
        }
        try {
            return redisTemplate.hasKey(key);
        } catch (RedisConnectionFailureException e) {
            triggerFallback("exists", e);
            return memoryGet(key) != null;
        } catch (Exception e) {
            log.error("[CacheService] Redis exists check failed, key: {}, falling back", key, e);
            return memoryGet(key) != null;
        }
    }

    // ==================== 防重放攻击 ====================

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
        return useMemoryFallback ? "InMemory" : "Redis";
    }

    // ==================== 内部工具方法 ====================

    private String memoryGet(String key) {
        Object[] entry = memoryCache.get(key);
        if (entry == null) return null;
        long expireAt = (Long) entry[1];
        if (expireAt > 0 && System.currentTimeMillis() > expireAt) {
            memoryCache.remove(key);
            return null;
        }
        return (String) entry[0];
    }

    private Boolean memoryAcquireLock(String lockKey, long timeout, TimeUnit unit) {
        long now = System.currentTimeMillis();
        long expireAt = timeout > 0 ? now + unit.toMillis(timeout) : now + 60000L;
        Long existing = memoryLocks.putIfAbsent(lockKey, expireAt);
        if (existing == null) return true;
        if (existing > 0 && now > existing) {
            memoryLocks.put(lockKey, expireAt);
            return true;
        }
        return false;
    }

    private void triggerFallback(String op, Exception e) {
        if (!useMemoryFallback) {
            log.warn("[CacheService] 触发 Redis → 内存缓存降级（{} 失败：{}）", op, e.getMessage());
            useMemoryFallback = true;
        }
    }
}
