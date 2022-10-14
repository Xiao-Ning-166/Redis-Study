package com.example.controller;


import com.example.dto.Result;
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

    @PostMapping("seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        return Result.fail("功能未完成");
    }
}
