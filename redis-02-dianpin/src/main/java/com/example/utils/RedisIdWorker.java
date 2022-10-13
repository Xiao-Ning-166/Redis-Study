package com.example.utils;

import javafx.scene.input.DataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 基于Redis实现全局唯一id生成器
 *
 * @author xiaoning
 * @date 2022/10/13
 */
@Component
public class RedisIdWorker {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 序列号部分的bit数
     */
    private static final int NUMBER_BIT = 32;

    /**
     * 得到全局唯一id
     *
     * @param keyPrefix 存储redis的key
     * @return
     */
    public long getId(String keyPrefix) {
        // 1、时间戳
        long timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

        // 2、获取Redis自增序列号
        // 2.1、获取当前日期精确到天
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        // 2.2、获取自增序列号
        long number = stringRedisTemplate.opsForValue().increment("id:" + keyPrefix + ":" + date);

        // 3、拼接
        return timestamp << NUMBER_BIT | number;
    }

}
