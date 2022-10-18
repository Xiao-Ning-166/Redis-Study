package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dto.Result;
import com.example.entity.Follow;
import com.example.mapper.FollowMapper;
import com.example.service.IFollowService;
import com.example.utils.UserHolder;
import org.springframework.stereotype.Service;

/**
 * @author xiaoning
 * @date 2022/10/18
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    /**
     * 关注/取关用户
     *
     * @param id       用户id
     * @param isFollow 关注/取关
     * @return
     */
    @Override
    public Result follow(Long id, Boolean isFollow) {
        // 1、获取当前登录用户
        Long userId = UserHolder.getUser().getId();

        // 2、判断是关注还是取关
        if (isFollow) {
            // 关注，新增数据
            Follow follow = new Follow();
            follow.setFollowUserId(id);
            follow.setUserId(userId);
            save(follow);
        } else {
            // 取关，删除数据
            remove(new QueryWrapper<Follow>().eq("user_id", userId).eq("follow_user_id", id));
        }
        return Result.ok();
    }

    /**
     * 是否关注用户
     *
     * @param id 用户id
     * @return
     */
    @Override
    public Result isFollow(Long id) {
        // 1、获取当前登录用户id
        Long userId = UserHolder.getUser().getId();

        // 2、查询当前登录用户是否关注该用户
        Integer count = query().eq("user_id", userId).eq("follow_user_id", id).count();
        return Result.ok(count > 0);
    }
}
