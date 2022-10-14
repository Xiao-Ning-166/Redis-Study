package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.SeckillVoucher;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀优惠券信息
 *
 * @author xiaoning
 * @date 2022/10/13
 */
@Mapper
public interface SeckillVoucherMapper extends BaseMapper<SeckillVoucher> {
}
