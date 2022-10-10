package com.example.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dto.Result;
import com.example.entity.ShopType;
import com.example.mapper.ShopTypeMapper;
import com.example.service.IShopTypeService;
import com.example.utils.RedisConstants;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaoning
 * @date 2022/10/09
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询商铺类型信息。先从缓存中查，没有再从数据库查
     *
     * @return
     */
    @Override
    public Result listByCache() {
        // 1、从缓存中查询商铺类型信息
        String shopTypeKey = RedisConstants.CACHE_SHOP_TYPE_KEY;
        Object shopTypeListJson = redisTemplate.opsForValue().get(shopTypeKey);

        if (shopTypeListJson != null) {
            // 2、存在，直接返回
            // List<ShopType> shopTypesByCache = JSONUtil.toList(shopTypeListJson.toString(), ShopType.class);
            List<ShopType> shopTypesByCache = (List<ShopType>) shopTypeListJson;

            return Result.ok(shopTypesByCache);
        }

        // 3、不存在，去数据库查询
        List<ShopType> shopTypeListByDB = query().orderByAsc("sort").list();
        if (CollectionUtil.isEmpty(shopTypeListByDB)) {
            // 4、不存在相关信息，返回错误信息
            return Result.fail("不存在商铺类型信息！");
        }

        // 5、存在，存储redis中
        redisTemplate.opsForValue().set(shopTypeKey, shopTypeListByDB);

        // 6、返回数据
        return Result.ok(shopTypeListByDB);
    }
}
