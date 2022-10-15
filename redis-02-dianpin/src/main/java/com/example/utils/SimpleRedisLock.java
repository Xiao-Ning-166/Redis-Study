package com.example.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 简易的Redis实现分布式锁
 *
 * @author xiaoning
 * @date 2022/10/15
 */
public class SimpleRedisLock implements ILock {

    private StringRedisTemplate stringRedisTemplate;

    /**
     * 业务名称。用于组成存储Redis的key
     */
    private String serviceName;

    public SimpleRedisLock(StringRedisTemplate stringRedisTemplate, String serviceName) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.serviceName = serviceName;
    }

    /**
     * 用于标识当前线程的前缀
     */
    private static final String ID_PREFIX = UUID.fastUUID().toString(true) + "-";

    /**
     * 释放锁的脚本
     */
    private static final DefaultRedisScript UNLOCK_SCRIPT;

    /**
     * 初始化释放锁的脚本
     */
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript();
        // 从类路径加载释放锁的脚本
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("lua/unlock.lua"));
        // 设置返回值类型
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    /**
     * 尝试获取锁
     *
     * @param timeout 超时时间。单位：秒
     * @return true：获取成功；false：获取失败
     */
    @Override
    public boolean tryLock(Long timeout) {
        // 1、组装key
        String key = RedisConstants.LOCK_PREFIX + serviceName + ":";

        // 2、获取线程标识
        String threadId = ID_PREFIX + Thread.currentThread().getName();

        // 3、获取锁
        Boolean isLock = stringRedisTemplate.opsForValue().setIfAbsent(key, threadId, timeout, TimeUnit.SECONDS);

        // 4、转换为基本类型，避免出现空指针
        return Boolean.TRUE.equals(isLock);
    }

    /**
     * 释放锁
     */
    @Override
    public void unlock() {
        // key和线程标识
        String key = RedisConstants.LOCK_PREFIX + serviceName + ":";
        String threadId = ID_PREFIX + Thread.currentThread().getName();
        // 调用lua脚本进行释放锁的操作
        stringRedisTemplate.execute(UNLOCK_SCRIPT, Arrays.asList(key), threadId);
    }
}
