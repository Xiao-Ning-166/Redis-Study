package com.example.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dto.Result;
import com.example.entity.Blog;
import com.example.entity.User;
import com.example.mapper.BlogMapper;
import com.example.service.IBlogService;
import com.example.service.IUserService;
import com.example.utils.RedisConstants;
import com.example.utils.SystemConstants;
import com.example.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xiaoning
 * @date 2022/10/09
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Resource
    private IUserService userService;

    /**
     * 通过id查询博客
     *
     * @param id
     * @return
     */
    @Override
    public Result queryBlogById(Long id) {
        // 1、查询博客信息
        Blog blog = blogMapper.queryBlogById(id);
        if (ObjectUtils.isEmpty(blog)) {
            return Result.fail("博客信息不存在！");
        }

        // 2、查询当前用户是否对博客点赞
        queryBlogLiked(blog);

        return Result.ok(blog);
    }

    /**
     * 查询博客是否被当前用户点赞
     *
     * @param blog 博客信息
     */
    private void queryBlogLiked(Blog blog) {
        // 1、获取当前登录用户的id
        Long userId = UserHolder.getUser().getId();
        // 2、判断当前用户是否已经点赞
        String key = RedisConstants.BLOG_LIKED_PREFIX + blog.getId();
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId);

        blog.setIsLike(BooleanUtil.isTrue(isMember));
    }

    /**
     * 修改博客的点赞信息
     *
     * @param id 博客id
     * @return
     */
    @Override
    public Result updateBlogLike(Long id) {
        // 1、获取当前登录用户的id
        Long userId = UserHolder.getUser().getId();
        // 2、判断当前用户是否已经点赞
        String key = RedisConstants.BLOG_LIKED_PREFIX + id;
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId);
        if (BooleanUtil.isFalse(isMember)) {
            // 3、未点赞，进行点赞
            // 3.1、数据库博客点赞数量+1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            if (isSuccess) {
                // 3.2、更新成功，将用户id保存到博客点赞集合中
                redisTemplate.opsForSet().add(key, userId);
            }
        } else {
            // 4、已点赞，取消点赞
            // 4.1、数据库中博客点赞数量-1
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
            if (isSuccess) {
                // 4.2、将用户从博客点赞集合中移除
                redisTemplate.opsForSet().remove(key, userId);
            }
        }
        return Result.ok();
    }

    /**
     * 查询热点博客
     *
     * @param current
     * @return
     */
    @Override
    public Result queryHotBlog(Integer current) {
        // 根据用户查询
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog -> {
            Long userId = blog.getUserId();
            User user = userService.getById(userId);
            blog.setName(user.getNickName());
            blog.setIcon(user.getIcon());

            // 查询博客是否被当前用户点赞
            queryBlogLiked(blog);
        });
        return Result.ok(records);
    }
}
