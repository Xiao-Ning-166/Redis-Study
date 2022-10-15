package com.example.utils;

/**
 * Redis常量
 *
 * @author xiaoning
 * @date 2022/10/08
 */
public class RedisConstants {

    /**
     * 登录验证码的前缀
     */
    public static final String LOGIN_CODE_PREFIX = "login:code:";

    /**
     * 登录验证码的有效时间。单位：分钟
     */
    public static final Long LOGIN_CODE_TTL = 5L;

    /**
     * 存储用户登录的前缀
     */
    public static final String LOGIN_TOKEN_PREFIX = "login:token:";

    /**
     * 用户token的有效时间。单位：分钟
     */
    public static final Long TOKEN_TTL = 30L;

    /**
     * shop商铺缓存Redis的前缀
     */
    public static final String CACHE_SHOP_PREFIX = "cache:shop:";

    /**
     * shop商铺缓存的有效期。单位：分钟
     */
    public static final Long CACHE_SHOP_TTL = 30L;

    /**
     * shop-type商铺类型缓存Redis的key
     */
    public static final String CACHE_SHOP_TYPE_KEY = "cache:shop-type:list";

    /**
     * shop-type商铺类型缓存Redis的有效期。单位：分钟
     */
    public static final Long CACHE_SHOP_TYPE_TTL = 60L;

    /**
     * 缓存空值的有效期。单位：分钟
     */
    public static final Long CACHE_NULL_TTL = 1L;

    /**
     * 互斥锁的有效期。单位：秒
     */
    public static final Long CACHE_MUTEX_LOCK_TTL = 10L;

    /**
     * 商铺互斥锁的前缀
     */
    public static final String LOCK_SHOP_PREFIX = "lock:shop:";

    /**
     * 分布式锁的前缀
     */
    public static final String LOCK_PREFIX = "lock:";
}
