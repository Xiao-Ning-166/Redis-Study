package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.VoucherOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券订单持久层
 *
 * @author xiaoning
 * @date 2022/10/14
 */
@Mapper
public interface VoucherOrderMapper extends BaseMapper<VoucherOrder> {
}
