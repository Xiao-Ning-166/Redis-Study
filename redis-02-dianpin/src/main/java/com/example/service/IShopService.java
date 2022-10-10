package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dto.Result;
import com.example.entity.Shop;

/**
 * 商户服务接口
 *
 * @author xiaoning
 * @date 2022/10/09
 */
public interface IShopService extends IService<Shop> {

    /**
     * 根据id查询商铺信息。根据id查询商铺信息。先从缓存中查，缓存没有再去数据库
     *
     * @param id 商铺id
     * @return
     */
    Result queryShopById(Long id);

    /**
     * 修改商铺信息
     *
     * @param shop
     */
    Result<?> update(Shop shop);
}
