package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Blog;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author xiaoning
 * @date 2022/10/09
 */
@Mapper
public interface BlogMapper extends BaseMapper<Blog> {
}
