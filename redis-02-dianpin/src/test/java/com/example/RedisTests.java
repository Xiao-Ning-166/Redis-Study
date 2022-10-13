package com.example;

import com.example.entity.Shop;
import com.example.service.IShopService;
import com.example.service.impl.ShopServiceImpl;
import com.example.utils.RedisConstants;
import com.example.utils.RedisUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author xiaoning
 * @date 2022/10/12
 */
@SpringBootTest
public class RedisTests {

    @Autowired
    private ShopServiceImpl shopService;

    @Test
    void saveShop2Redis() throws InterruptedException {

        shopService.saveShop2Redis(1L, 20L);

    }

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testRedisUtils() throws InterruptedException {

        Shop shop = shopService.getById(1L);

        redisUtils.setWithLogic(RedisConstants.CACHE_SHOP_PREFIX + shop.getId(), shop,20L, TimeUnit.SECONDS);

    }
}
