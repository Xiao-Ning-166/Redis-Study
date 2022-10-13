package com.example.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 全局唯一ID生成器测试类
 *
 * @author xiaoning
 * @date 2022/10/13
 */
@SpringBootTest
class RedisIdWorkerTests {

    @Autowired
    private RedisIdWorker redisIdWorker;

    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(100, 150,
            30, TimeUnit.SECONDS, new LinkedBlockingDeque<>(50));

    @Test
    void getId() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(100);

        long begin = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            threadPoolExecutor.submit(() -> {
                for (int j = 0; j < 100; j++) {
                    System.out.println("id ==>> " + redisIdWorker.getId("test"));
                }
                countDownLatch.countDown();
            });
        }

        // 等到所有线程执行完毕
        countDownLatch.await();
        System.out.println("总耗时 ==>> " + (System.currentTimeMillis() - begin));
    }
}
