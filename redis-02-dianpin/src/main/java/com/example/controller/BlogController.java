package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dto.Result;
import com.example.dto.UserDTO;
import com.example.entity.Blog;
import com.example.entity.User;
import com.example.service.IBlogService;
import com.example.service.IUserService;
import com.example.utils.SystemConstants;
import com.example.utils.UserHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 博客控制器
 *
 * @author xiaoning
 * @date 2022/10/09
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private IBlogService blogService;

    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        // 保存探店博文
        blogService.save(blog);
        // 返回id
        return Result.ok(blog.getId());
    }

    /**
     * 查看博客信息
     *
     * @param id 博客id
     * @return
     */
    @GetMapping("/{id}")
    public Result getBlogInfo(@PathVariable("id") Long id) {
        return blogService.queryBlogById(id);
    }

    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id) {
        return blogService.updateBlogLike(id);
    }

    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", user.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    /**
     * 查询热点博客
     *
     * @param current
     * @return
     */
    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.queryHotBlog(current);
    }

    /**
     * 查询博客前5个点赞的用户信息
     *
     * @param id 博客id
     * @return
     */
    @GetMapping("/likes/{id}")
    public Result getTop5Liked(@PathVariable("id") Long id) {
        return blogService.getTop5Liked(id);
    }
}
