package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author xiaoning
 * @date 2022/10/08
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
