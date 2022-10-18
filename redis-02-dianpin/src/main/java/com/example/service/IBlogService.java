package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dto.Result;
import com.example.entity.Blog;

/**
 * 博客服务
 *
 * @author xiaoning
 * @date 2022/10/09
 */
public interface IBlogService extends IService<Blog> {

    /**
     * 通过id查询博客
     *
     * @param id
     * @return
     */
    Result queryBlogById(Long id);

    /**
     * 修改博客的点赞信息
     *
     * @param id
     * @return
     */
    Result updateBlogLike(Long id);

    /**
     * 查询热点博客
     *
     * @param current
     * @return
     */
    Result queryHotBlog(Integer current);
}
