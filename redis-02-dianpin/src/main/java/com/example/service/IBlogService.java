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

    /**
     * 查询博客前5个点赞的用户信息
     *
     * @param id 博客id
     * @return
     */
    Result getTop5Liked(Long id);

    /**
     * 分页查询用户博客信息
     *
     * @param userId  用户id
     * @param current 当前页码。默认为1
     * @return
     */
    Result queryUserBlog(Long userId, Integer current);
}
