package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Blog;
import com.example.mapper.BlogMapper;
import com.example.service.IBlogService;
import org.springframework.stereotype.Service;

/**
 * @author xiaoning
 * @date 2022/10/09
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {
}
