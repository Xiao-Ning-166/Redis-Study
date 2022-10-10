package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dto.Result;
import com.example.entity.Shop;
import com.example.entity.ShopType;

/**
 * @author xiaoning
 * @date 2022/10/09
 */
public interface IShopTypeService extends IService<ShopType> {

    /**
     * 查询商铺类型信息。先从缓存中查，没有再从数据库查
     *
     * @return
     */
    Result listByCache();

}
