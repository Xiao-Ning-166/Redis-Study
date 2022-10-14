package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dto.Result;
import com.example.entity.SeckillVoucher;
import com.example.entity.Voucher;
import com.example.mapper.VoucherMapper;
import com.example.service.ISeckillVoucherService;
import com.example.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 优惠券服务实现类
 *
 * @author xiaoning
 * @date 2022/10/13
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Autowired
    private VoucherMapper voucherMapper;

    @Autowired
    private ISeckillVoucherService seckillVoucherService;

    /**
     * 新增秒杀优惠券
     *
     * @param voucher
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addSeckillVoucher(Voucher voucher) {
        // 1、保存优惠券
        save(voucher);
        // 2、保存秒杀优惠券信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
    }

    /**
     * 根据店铺查询优惠券信息
     *
     * @param shopId 商铺id
     * @return
     */
    @Override
    public Result queryVoucherOfShop(Long shopId) {
        List<Voucher> list = voucherMapper.queryVoucherOfShop(shopId);
        return Result.ok(list);
    }
}
