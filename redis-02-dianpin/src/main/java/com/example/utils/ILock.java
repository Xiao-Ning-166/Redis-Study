package com.example.utils;

/**
 * 分布式锁接口
 *
 * @author xiaoning
 * @date 2022/10/15
 */
public interface ILock {

    /**
     * 尝试获取锁
     *
     * @param timeout 超时时间。单位：秒
     * @return true：获取成功；false：获取失败
     */
    boolean tryLock(Long timeout);

    /**
     * 释放锁
     */
    void unlock();

}
