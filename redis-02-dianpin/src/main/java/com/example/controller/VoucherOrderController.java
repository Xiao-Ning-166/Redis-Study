package com.example.controller;


import com.example.dto.Result;
import com.example.service.IVoucherOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * 优惠券订单控制器
 *
 * @author xiaoning
 * @date 2022/10/08
 */
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    @Autowired
    private IVoucherOrderService voucherOrderService;

    @PostMapping("seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        // 抢购秒杀优惠券（同步执行）
        // return voucherOrderService.seckillVoucherSync(voucherId);

        // 抢购秒杀优惠券（异步执行）
        return voucherOrderService.seckillVoucherAsync(voucherId);
    }
}
