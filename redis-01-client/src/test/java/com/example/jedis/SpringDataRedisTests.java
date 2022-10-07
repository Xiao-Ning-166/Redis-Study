package com.example.jedis;

import com.example.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author xiaoning
 * @date 2022/10/07
 */
@SpringBootTest
public class SpringDataRedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testString() {
        // 存储数据
        redisTemplate.opsForValue().set("spring:name:1", "赵四", 30, TimeUnit.SECONDS);
        // 读数据
        String name = (String) redisTemplate.opsForValue().get("spring:name:1");
        System.out.println(name);
    }

    @Test
    void testHash() {
        // 存储数据
        redisTemplate.opsForHash().put("spring:user:1", "name", "狗剩子");
        redisTemplate.opsForHash().put("spring:user:1", "age", 18);
        // 读数据
        System.out.println(redisTemplate.opsForHash().get("spring:user:1", "name"));
        System.out.println(redisTemplate.opsForHash().get("spring:user:1", "age"));
    }

    /**
     * 测试序列化的两种方式
     *
     * @throws JsonProcessingException
     */
    @Test
    void test03() throws JsonProcessingException {
        // 方式一：存储数据
        // 存数据
        redisTemplate.opsForValue().set("serializer:user:1", new User("张三", 24));
        // 读数据
        Object o = redisTemplate.opsForValue().get("serializer:user:1");
        System.out.println("方式一：" + o);

        // 方式二：
        // 存储数据
        ObjectMapper objectMapper = new ObjectMapper();
        String user2json = objectMapper.writeValueAsString(new User("李四", 30));
        stringRedisTemplate.opsForValue().set("serializer:user:2", user2json);
        // 读数据
        String json2user = stringRedisTemplate.opsForValue().get("serializer:user:2");
        User user = objectMapper.readValue(json2user, User.class);
        System.out.println("方式二：" + user);
    }
}
