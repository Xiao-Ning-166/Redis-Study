package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dto.Result;
import com.example.entity.Voucher;

/**
 * 优惠券服务类
 *
 * @author xiaoning
 * @date 2022/10/13
 */
public interface IVoucherService extends IService<Voucher> {

    /**
     * 新增秒杀优惠券
     *
     * @param voucher
     */
    void addSeckillVoucher(Voucher voucher);

    /**
     * 根据店铺查询优惠券信息
     *
     * @param shopId 商铺id
     * @return
     */
    Result queryVoucherOfShop(Long shopId);
}
