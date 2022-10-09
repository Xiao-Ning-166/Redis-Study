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

}
