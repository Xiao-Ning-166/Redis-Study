package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Shop;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商铺持久层
 *
 * @author xiaoning
 * @date 2022/10/09
 */
@Mapper
public interface ShopMapper extends BaseMapper<Shop> {
}
