package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dto.Result;
import com.example.entity.VoucherOrder;

/**
 * 优惠券订单服务
 *
 * @author xiaoning
 * @date 2022/10/14
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    /**
     * 抢购秒杀优惠券
     *
     * @param voucherId
     * @return
     */
    Result seckillVoucher(Long voucherId);
}
