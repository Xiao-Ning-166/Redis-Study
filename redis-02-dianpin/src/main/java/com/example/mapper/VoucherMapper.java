package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Voucher;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 优惠券持久层
 *
 * @author xiaoning
 * @date 2022/10/13
 */
@Mapper
public interface VoucherMapper extends BaseMapper<Voucher> {

    /**
     * 根据商铺id查询商铺优惠券信息
     *
     * @param shopId
     * @return
     */
    List<Voucher> queryVoucherOfShop(Long shopId);
}
