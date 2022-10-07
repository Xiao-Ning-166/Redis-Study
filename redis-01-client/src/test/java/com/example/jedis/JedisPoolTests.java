package com.example.jedis;

import com.example.jedis.utils.JedisConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

/**
 * 测试jedis连接池
 *
 * @author xiaoning
 * @date 2022/10/07
 */
public class JedisPoolTests {

    private Jedis jedisClient;

    @BeforeEach
    void setUp() {
        // 1、从连接池中获取连接
        this.jedisClient = JedisConnectionFactory.getJedis();
    }

    @Test
    void testString() {
        // 2、操作redis
        jedisClient.set("jedis:name:2", "wangwu");
        System.out.println(jedisClient.get("jedis:name:2"));
        jedisClient.expire("jedis:name:1", 10L);
        jedisClient.expire("jedis:name:2", 10L);
    }

    @AfterEach
    void tearDown() {
        // 释放连接
        if (jedisClient != null) {
            jedisClient.close();
        }
    }
}
