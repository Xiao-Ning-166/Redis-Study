package com.example.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.dto.Result;
import com.example.entity.Shop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Redis工具类
 *
 * @author xiaoning
 * @date 2022/10/12
 */
@Component
public class RedisUtils {

    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisUtils(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 线程池。
     */
    private static final ThreadPoolExecutor CACHE_REBUILD_EXECUTOR =
            new ThreadPoolExecutor(5, 10, 30,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));

    /**
     * 向Redis中缓存数据
     *
     * @param key      缓存数据的key
     * @param data     缓存的数据
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    public void set(String key, Object data, Long timeout, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(data), timeout, timeUnit);
    }

    /**
     * 从Redis中获取数据
     *
     * @param key      缓存数据的key
     * @param data     缓存的数据
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    public void setWithLogic(String key, Object data, Long timeout, TimeUnit timeUnit) {
        // 1、封装逻辑过期时间对象
        RedisData redisData = new RedisData();
        redisData.setData(data);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(timeUnit.toSeconds(timeout)));

        // 2、写入Redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 从缓存中获取数据，防止缓存穿透
     *
     * @param keyPrefix 前缀
     * @param id
     * @param type      数据的类型
     * @param loadDb    未命中缓存时，查询数据库的方法
     * @param timeout   过期时间
     * @param timeUnit  时间单位
     * @param <T>       返回值类型
     * @param <ID>      id类型
     * @return
     */
    public <T, ID> T getWithCachePenetrate(String keyPrefix, ID id, Class<T> type, Function<ID, T> loadDb,
                                           Long timeout, TimeUnit timeUnit) {
        // 1、从缓存中查询商铺信息
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);

        // 2、判断是否存在
        if (StrUtil.isNotBlank(json)) {
            // 3、存在，直接返回
            // 3.1、反序列化为对象
            T json2Bean = JSONUtil.toBean(json, type);
            return json2Bean;
        }

        // 判断是否是空对象
        if ("".equals(json)) {
            // 空对象，返回错误信息
            return null;
        }

        // 4、不存在，从数据库中查询
        T data = loadDb.apply(id);

        if (data == null) {
            // 5、不存在
            // 5.1、缓存空值
            stringRedisTemplate.opsForValue().set(key, "", timeout, timeUnit);
            // 5.2、返回错误信息
            return null;
        }

        // 6、存在，存入缓存中
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(data), timeout, timeUnit);

        // 7、返回商铺信息
        return data;
    }


    /**
     * 逻辑过期时间解决缓存击穿
     *
     * @param id
     * @return
     */
    public <T, ID> T getWithLogicExpire(String keyPrefix, ID id, Class<T> type, Function<ID, T> loadDb,
                                        Long timeout, TimeUnit timeUnit) {
        // 1、从缓存中查询
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);

        // 2、判断是否存在
        if (StrUtil.isBlank(json)) {
            // 3、不存在，直接返回
            return null;
        }

        // 4、判断是否过期
        RedisData json2Bean = JSONUtil.toBean(json, RedisData.class);
        T data = JSONUtil.toBean((JSONObject) json2Bean.getData(), type);
        LocalDateTime expireTime = json2Bean.getExpireTime();
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 4.1、未过期，直接返回
            return data;
        }

        // 5、过期，重建缓存
        // 5.1、获取互斥锁
        String lockKey = RedisConstants.LOCK_SHOP_PREFIX + id;
        boolean isLock = tryLock(lockKey);
        // 5.2、判断获取互斥锁是否成功
        if (isLock) {
            // 5.3、开启独立线程，重建缓存
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 1、重建缓存
                    // 查询数据库
                    T bean = loadDb.apply(id);
                    // 向Redis写入数据
                    this.setWithLogic(key, bean, timeout, timeUnit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 2、释放互斥锁
                    unlock(lockKey);
                }
            });
        }

        // 6、返回商铺信息
        return data;
    }


    /**
     * 获取互斥锁
     *
     * @param key
     * @return
     */
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", RedisConstants.CACHE_MUTEX_LOCK_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 释放锁
     *
     * @param key
     */
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
