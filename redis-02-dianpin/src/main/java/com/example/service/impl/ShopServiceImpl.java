package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dto.Result;
import com.example.entity.Shop;
import com.example.mapper.ShopMapper;
import com.example.service.IShopService;
import com.example.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 商铺服务实现类
 *
 * @author xiaoning
 * @date 2022/10/09
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据id查询商铺信息。先从缓存中查，缓存没有再去数据库
     *
     * @param id 商铺id
     * @return
     */
    @Override
    public Result queryShopById(Long id) {
        // 缓存穿透解决方案
        // return queryShopWithCachePenetrate(id);

        // 基于互斥锁的缓存击穿解决方案
        return queryShopWithCacheBreakdownByMutex(id);
    }

    /**
     * 基于互斥锁的缓存击穿解决方案
     *
     * @param id
     * @return
     */
    public Result queryShopWithCacheBreakdownByMutex(Long id) {
        // 1、从缓存中查询商铺信息
        String shopKey = RedisConstants.CACHE_SHOP_PREFIX + id;
        Map shopMap = redisTemplate.opsForHash().entries(shopKey);

        // 2、判断是否存在
        if (!shopMap.isEmpty()) {
            // 3、存在，直接返回
            Shop shop = BeanUtil.fillBeanWithMap(shopMap, new Shop(), false);
            return Result.ok(shop);
        }

        // 4、判断是否是空对象
        if ("".equals(shopMap.get(""))) {
            // 空对象，返回错误信息
            return Result.fail("商铺信息不存在");
        }

        // 5、实现缓存重建
        // 5.1、获取互斥锁
        String lockKey = RedisConstants.LOCK_SHOP_PREFIX + id;
        Shop shop = null;
        try {
            boolean isLock = tryLock(lockKey);

            // 5.2、判断互斥锁获取是否成功
            if (!isLock) {
                // 5.3、失败，休眠一段时间，重试
                Thread.sleep(50);
                // 重试
                return queryShopWithCacheBreakdownByMutex(id);
            }

            // 5.4、成功，从数据库查询数据
            shop = getById(id);
            // 延时，模拟重建
            Thread.sleep(200);

            // 5.5、判断是否存在
            if (shop == null) {
                // 5.5.1、不存在，缓存空值
                Map shopOfNull = new HashMap();
                shopOfNull.put("", "");
                redisTemplate.opsForHash().putAll(shopKey, shopOfNull);
                redisTemplate.expire(shopKey, RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                // 5.5.2、返回错误信息
                return Result.fail("商铺信息不存在！");
            }

            // 5.6、存在，存入缓存中
            Map<String, Object> shop2Map = BeanUtil.beanToMap(shop);
            redisTemplate.opsForHash().putAll(shopKey, shop2Map);
            redisTemplate.expire(shopKey, RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            new RuntimeException(e);
        } finally {
            // 6、释放互斥锁
            unlock(lockKey);
        }


        // 7、返回商铺信息
        if (shop == null) {
            return Result.fail("店铺信息不存在！");
        }
        return Result.ok(shop);
    }

    /**
     * 获取互斥锁
     *
     * @param key
     * @return
     */
    private boolean tryLock(String key) {
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(key, "1", RedisConstants.CACHE_MUTEX_LOCK_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 释放锁
     *
     * @param key
     */
    private void unlock(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 解决缓存穿透
     *
     * @param id
     * @return
     */
    public Result queryShopWithCachePenetrate(Long id) {
        // 1、从缓存中查询商铺信息
        String shopKey = RedisConstants.CACHE_SHOP_PREFIX + id;
        Map shopMap = redisTemplate.opsForHash().entries(shopKey);

        // 2、判断是否存在
        if (!shopMap.isEmpty()) {
            // 3、存在，直接返回
            Shop shop = BeanUtil.fillBeanWithMap(shopMap, new Shop(), false);
            return Result.ok(shop);
        }

        // 判断是否是空对象
        if ("".equals(shopMap.get(""))) {
            // 空对象，返回错误信息
            return Result.fail("商铺信息不存在");
        }

        // 4、不存在，从数据库中查询
        Shop shop = getById(id);

        if (shop == null) {
            // 5、不存在
            // 5.1、缓存空值
            Map shopOfNull = new HashMap();
            shopOfNull.put("", "");
            redisTemplate.opsForHash().putAll(shopKey, shopOfNull);
            redisTemplate.expire(shopKey, RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            // 5.2、返回错误信息
            return Result.fail("商铺信息不存在！");
        }

        // 6、存在，存入缓存中
        Map<String, Object> shop2Map = BeanUtil.beanToMap(shop);
        redisTemplate.opsForHash().putAll(shopKey, shop2Map);
        redisTemplate.expire(shopKey, RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 7、返回商铺信息
        return Result.ok(shop);
    }

    /**
     * 修改商铺信息
     *
     * @param shop
     */
    @Override
    @Transactional
    public Result<?> update(Shop shop) {
        // 1、判断id是否存在
        Long shopId = shop.getId();
        if (shopId == null) {
            return Result.fail("商铺id不能为空！");
        }

        // 2、更新数据库
        updateById(shop);

        // 3、删除缓存中的数据
        redisTemplate.delete(RedisConstants.CACHE_SHOP_PREFIX + shopId);

        return Result.ok();
    }
}
