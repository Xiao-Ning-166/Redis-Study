package com.example.controller;

import com.example.dto.Result;
import com.example.service.IFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 关注
 *
 * @author xiaoning
 * @date 2022/10/18
 */
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private IFollowService followService;

    /**
     * 关注/取关用户
     *
     * @param id       关注/取关用户的id
     * @param isFollow true：关注、false：取关
     * @return
     */
    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") Long id, @PathVariable("isFollow") Boolean isFollow) {
        return followService.follow(id, isFollow);
    }


    /**
     * 查询当前用户是否关注某用户
     *
     * @param id 用户的id
     * @return
     */
    @GetMapping("/or/not/{id}")
    public Result isFollow(@PathVariable("id") Long id) {
        return followService.isFollow(id);
    }

    /**
     * 查询当前登录用户和目标用户的共同关注用户
     *
     * @param targetUserId 目标用户
     * @return
     */
    @GetMapping("/common/{id}")
    public Result followCommons(@PathVariable("id") Long targetUserId) {
        return followService.followCommons(targetUserId);
    }

}
