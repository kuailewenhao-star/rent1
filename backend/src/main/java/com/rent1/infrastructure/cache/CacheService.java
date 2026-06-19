package com.rent1.infrastructure.cache;

import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 * 支持 Redis / 内存两种实现，自动根据环境选择
 */
public interface CacheService {

    /**
     * 设置缓存
     */
    void set(String key, String value, long timeout, TimeUnit unit);

    /**
     * 获取缓存
     */
    String get(String key);

    /**
     * 删除缓存
     */
    Boolean delete(String key);

    /**
     * 获取分布式锁（原子性：仅在key不存在时才设置）
     */
    Boolean acquireLock(String lockKey, long timeout, TimeUnit unit);

    /**
     * 释放分布式锁
     */
    void releaseLock(String lockKey);

    /**
     * 判断 key 是否存在
     */
    Boolean exists(String key);

    /**
     * 判断 nonce 是否存在（防重放攻击）
     */
    Boolean nonceExists(String nonce);

    /**
     * 存储 nonce（防重放攻击）
     */
    void storeNonce(String nonce, long expireSeconds);

    /**
     * 当前使用的缓存实现名称（Redis / InMemory）
     */
    String getImplementationName();
}
