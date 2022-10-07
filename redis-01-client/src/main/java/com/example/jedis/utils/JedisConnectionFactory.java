package com.example.jedis.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * jedis连接池
 *
 * @author xiaoning
 * @date 2022/10/07
 */
public class JedisConnectionFactory {

    /**
     * jedis连接池对象
     */
    private static final JedisPool jedisPool;


    static {
        // 1、配置连接池
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        // 1.1、设置最大连接数
        poolConfig.setMaxTotal(8);
        // 1.2、设置最大空闲连接数
        poolConfig.setMaxIdle(8);
        // 1.3、设置最少空闲连接数
        poolConfig.setMinIdle(2);
        // 1.4、设置最长等待时间
        poolConfig.setMaxWaitMillis(1000L);

        // 2、创建连接池
        jedisPool = new JedisPool(poolConfig, "127.0.0.1", 6379);
    }

    /**
     * 从连接池中获取一个连接
     *
     * @return
     */
    public static Jedis getJedis() {
        return jedisPool.getResource();
    }

}
