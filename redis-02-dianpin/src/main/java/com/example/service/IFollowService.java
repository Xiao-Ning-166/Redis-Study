package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dto.Result;
import com.example.entity.Follow;

/**
 * 关注服务
 *
 * @author xiaoning
 * @date 2022/10/18
 */
public interface IFollowService extends IService<Follow> {

    /**
     * 关注/取关用户
     *
     * @param id       用户id
     * @param isFollow 关注/取关
     * @return
     */
    Result follow(Long id, Boolean isFollow);

    /**
     * 是否关注用户
     *
     * @param id 用户id
     * @return
     */
    Result isFollow(Long id);

    /**
     * 查询当前登录用户和目标用户的共同关注用户
     *
     * @param targetUserId 目标用户
     * @return
     */
    Result followCommons(Long targetUserId);
}
