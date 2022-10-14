package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dto.Result;
import com.example.dto.UserDTO;
import com.example.entity.SeckillVoucher;
import com.example.entity.VoucherOrder;
import com.example.mapper.VoucherOrderMapper;
import com.example.service.ISeckillVoucherService;
import com.example.service.IVoucherOrderService;
import com.example.utils.RedisIdWorker;
import com.example.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @author xiaoning
 * @date 2022/10/14
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Autowired
    private ISeckillVoucherService seckillVoucherService;

    @Autowired
    private RedisIdWorker redisIdWorker;

    /**
     * 抢购秒杀优惠券
     *
     * @param voucherId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result seckillVoucher(Long voucherId) {
        // 1、获取优惠券信息
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);

        // 2、判断限时抢购是否开始
        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
            // 未开始，返回错误信息
            return Result.fail("抱歉，限时抢购还未开始！");
        }

        // 3、判断限时抢购是否结束
        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
            // 秒杀已经结束，返回错误信息
            return Result.fail("抱歉，限时抢购已经结束！");
        }

        // 4、判断库存是否充足
        if (seckillVoucher.getStock() < 1) {
            // 库存不足，返回错误信息
            return Result.fail("库存不足！");
        }

        // 5、扣库存
        boolean isSuccess = seckillVoucherService.update()
                // 扣减库存
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                // 判断库存是否大于0
                .gt("stock", 0).update();
        if (!isSuccess) {
            // 扣库存失败
            return Result.fail("库存不足！");
        }

        // 6、生成订单
        VoucherOrder voucherOrder = new VoucherOrder();
        // 6.1、设置订单id
        long voucherOrderId = redisIdWorker.getId("order");
        voucherOrder.setId(voucherOrderId);
        // 6.2、设置优惠券id
        voucherOrder.setVoucherId(voucherId);
        // 6.3、设置用户id
        UserDTO user = UserHolder.getUser();
        voucherOrder.setUserId(user.getId());
        // 6.4、保存订单信息
        save(voucherOrder);

        // 返回订单id
        return Result.ok(voucherOrder.getId());
    }
}
