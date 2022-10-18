package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dto.Result;
import com.example.dto.UserDTO;
import com.example.entity.Follow;
import com.example.entity.User;
import com.example.mapper.FollowMapper;
import com.example.service.IFollowService;
import com.example.service.IUserService;
import com.example.utils.RedisConstants;
import com.example.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xiaoning
 * @date 2022/10/18
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IUserService userService;

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

        String key = RedisConstants.FOLLOWS_USER_PREFIX + userId;

        // 2、判断是关注还是取关
        if (isFollow) {
            // 关注，新增数据
            Follow follow = new Follow();
            follow.setFollowUserId(id);
            follow.setUserId(userId);
            boolean isSuccess = save(follow);
            if (isSuccess) {
                // 保存到redis中
                redisTemplate.opsForSet().add(key, id.toString());
            }
        } else {
            // 取关，删除数据
            boolean isSuccess = remove(new QueryWrapper<Follow>().eq("user_id", userId).eq("follow_user_id", id));
            if (isSuccess) {
                // 从redis集合中删除
                redisTemplate.opsForSet().remove(key, id.toString());
            }
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

    /**
     * 查询当前登录用户和目标用户的共同关注用户
     *
     * @param targetUserId 目标用户
     * @return
     */
    @Override
    public Result followCommons(Long targetUserId) {
        // 1、当前登录用户id
        Long userId = UserHolder.getUser().getId();
        String key1 = RedisConstants.FOLLOWS_USER_PREFIX + userId;
        String key2 = RedisConstants.FOLLOWS_USER_PREFIX + targetUserId;

        // 2、查询交集
        Set<String> userIds = redisTemplate.opsForSet().intersect(key1, key2);

        // 3、解析id集合
        List<Long> ids = userIds.stream().map(Long::valueOf).collect(Collectors.toList());

        // 4、查询共同关注用户
        List<User> users = userService.listByIds(ids);
        if (CollectionUtil.isEmpty(users)) {
            return Result.ok(Collections.emptyList());
        }

        // 5、转换user
        List<UserDTO> userDTOS = users.stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class)).collect(Collectors.toList());
        return Result.ok(userDTOS);
    }
}
