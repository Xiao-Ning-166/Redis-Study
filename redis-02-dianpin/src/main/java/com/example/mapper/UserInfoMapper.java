package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author xiaoning
 * @date 2022/10/17
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}
