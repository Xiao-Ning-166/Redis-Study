package com.example.utils;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 逻辑过期解决缓存击穿问题的对象
 *
 * @author xiaoning
 * @date 2022/10/12
 */
@Data
public class RedisData<T> {

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 保存的数据
     */
    private T data;

}
