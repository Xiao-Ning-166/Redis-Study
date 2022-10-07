package com.example.jedis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

/**
 * @author xiaoning
 * @date 2022/10/07
 */
public class JedisTests {

    private Jedis jedisClient;

    private static final String PREFIX = "jedis";

    @BeforeEach
    public void setUp() {
        // 1、建立连接
        this.jedisClient = new Jedis("127.0.0.1", 6379);
    }

    @Test
    public void testString() {
        // 1、插入数据
        this.jedisClient.set(PREFIX + ":" + "name:1", "zhangsan");
        // 2、获取数据
        String name = this.jedisClient.get(PREFIX + ":" + "name:1");
        System.out.println(name);
    }

    @Test
    void testHash() {
        this.jedisClient.hset(PREFIX + ":user:1", "name", "lisi");
        this.jedisClient.hset(PREFIX + ":user:1", "age", "18");
    }

    @AfterEach
    public void tearDown() {
        // 1、释放资源
        if (this.jedisClient != null) {
            this.jedisClient.close();
        }
    }

}
