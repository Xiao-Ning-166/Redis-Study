package com.example.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

/**
 * @author xiaoning
 * @date 2022/10/16
 */
@Slf4j
@SpringBootTest
public class RedissonTests {

    @Autowired
    private RedissonClient redissonClient;

    // 获取锁对象
    RLock lock = null;

    @BeforeEach
    void setUp() {
        lock = redissonClient.getLock("lock:test");
    }

    /**
     * 测试可重入锁
     */
    @Test
    void testReLock() {

        // 尝试获取锁
        try {
            boolean isLock = lock.tryLock(10L, 30L, TimeUnit.SECONDS);
            if (!isLock) {
                // 获取锁失败
                log.error("1 获取锁失败");
                return;
            }
            // 调用方法2
            method2();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            log.error("1 释放锁");
            lock.unlock();
        }

    }

    public void method2() {
        try {
            // 尝试获取锁
            boolean isLock = lock.tryLock(10L, 30L, TimeUnit.SECONDS);
            if (!isLock) {
                log.error("2 获取锁失败");
            }
            log.error("2 获取锁成功");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            log.error("2 释放锁");
        }

    }
}
