package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.stream.CollectorUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dto.Result;
import com.example.dto.UserDTO;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        // 0、判断当前用户是否登录
        UserDTO userDTO = UserHolder.getUser();
        if (userDTO == null) {
            // 未登录，无需查询当前用户是否点赞博客
            return;
        }
        // 1、获取当前登录用户的id
        Long userId = userDTO.getId();
        // 2、判断当前用户是否已经点赞
        String key = RedisConstants.BLOG_LIKED_PREFIX + blog.getId();
        Double score = redisTemplate.opsForZSet().score(key, userId);

        blog.setIsLike(score != null);
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
        Double score = redisTemplate.opsForZSet().score(key, userId);
        if (score == null) {
            // 3、未点赞，进行点赞
            // 3.1、数据库博客点赞数量+1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            if (isSuccess) {
                // 3.2、更新成功，将用户id保存到博客点赞集合中，以当前时间戳为分数值
                redisTemplate.opsForZSet().add(key, String.valueOf(userId), System.currentTimeMillis());
            }
        } else {
            // 4、已点赞，取消点赞
            // 4.1、数据库中博客点赞数量-1
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
            if (isSuccess) {
                // 4.2、将用户从博客点赞集合中移除
                redisTemplate.opsForZSet().remove(key, String.valueOf(userId));
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

    /**
     * 查询博客前5个点赞的用户信息
     *
     * @param id 博客id
     * @return
     */
    @Override
    public Result getTop5Liked(Long id) {
        // 1、查询博客前5个点赞的用户id
        String key = RedisConstants.BLOG_LIKED_PREFIX + id;
        Set<String> userIds = redisTemplate.opsForZSet().range(key, 0, 4);

        // 2、判断用户集合是否为空
        if (CollectionUtil.isEmpty(userIds)) {
            // 说明没有人给博客点赞，直接返回
            return Result.ok(Collections.emptyList());
        }

        // 3、处理用户id集合
        // 3.1、将id拼接成以,分割的字符串
        List<Long> idList = userIds.stream().map(Long::valueOf).collect(Collectors.toList());
        String userIdStr = CollectionUtil.join(userIds, ",");
        List<UserDTO> userDTOList = userService.query()
                .in("id", idList).last("ORDER BY FIELD(id," + userIdStr + ")")
                .list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());

        // 4、返回结果
        return Result.ok(userDTOList);
    }

    /**
     * 分页查询用户博客信息
     *
     * @param userId  用户id
     * @param current 当前页码。默认为1
     * @return
     */
    @Override
    public Result queryUserBlog(Long userId, Integer current) {
        // 根据用户查询
        Page<Blog> page = query()
                .eq("user_id", userId).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog -> {
            Long id = blog.getUserId();
            User user = userService.getById(id);
            blog.setName(user.getNickName());
            blog.setIcon(user.getIcon());

            // 查询博客是否被当前用户点赞
            queryBlogLiked(blog);
        });
        return Result.ok(records);
    }

}
