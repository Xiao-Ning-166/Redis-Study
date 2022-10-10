package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dto.Result;
import com.example.entity.Shop;
import com.example.mapper.ShopMapper;
import com.example.service.IShopService;
import com.example.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

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
        // 1、从缓存中查询商铺信息
        String shopKey = RedisConstants.CACHE_SHOP_PREFIX + id;
        Map shopMap = redisTemplate.opsForHash().entries(shopKey);

        // 2、判断是否存在
        if (!shopMap.isEmpty()) {
            // 3、存在，直接返回
            Shop shop = BeanUtil.fillBeanWithMap(shopMap, new Shop(), false);
            return Result.ok(shop);
        }

        // 4、不存在，从数据库中查询
        Shop shop = getById(id);

        if (shop == null) {
            // 5、不存在，返回错误信息
            return Result.fail("商铺信息不存在！");
        }

        // 6、存在，存入缓存中
        Map<String, Object> shop2Map = BeanUtil.beanToMap(shop);
        redisTemplate.opsForHash().putAll(shopKey, shop2Map);

        // 7、返回商铺信息
        return Result.ok(shop);
    }
}
