package com.rent1.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 设置缓存
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("Redis set failed, key: {}", key, e);
        }
    }

    /**
     * 获取缓存
     */
    public String get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis get failed, key: {}", key, e);
            return null;
        }
    }

    /**
     * 删除缓存
     */
    public Boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis delete failed, key: {}", key, e);
            return false;
        }
    }

    /**
     * 设置分布式锁
     */
    public Boolean acquireLock(String lockKey, long timeout, TimeUnit unit) {
        try {
            Boolean result = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "LOCKED", timeout, unit);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis acquire lock failed, key: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 释放分布式锁
     */
    public void releaseLock(String lockKey) {
        try {
            redisTemplate.delete(lockKey);
        } catch (Exception e) {
            log.error("Redis release lock failed, key: {}", lockKey, e);
        }
    }

    /**
     * 判断key是否存在
     */
    public Boolean exists(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis exists check failed, key: {}", key, e);
            return false;
        }
    }

    /**
     * 判断nonce是否存在（防重放攻击）
     */
    public Boolean nonceExists(String nonce) {
        return exists("nonce:" + nonce);
    }

    /**
     * 存储nonce（防重放攻击）
     */
    public void storeNonce(String nonce, long expireSeconds) {
        set("nonce:" + nonce, "1", expireSeconds, TimeUnit.SECONDS);
    }
}
