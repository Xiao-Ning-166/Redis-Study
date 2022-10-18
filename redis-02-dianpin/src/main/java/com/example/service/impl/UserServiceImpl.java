package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dto.LoginFormDTO;
import com.example.dto.Result;
import com.example.dto.UserDTO;
import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.service.IUserService;
import com.example.utils.RedisConstants;
import com.example.utils.RegexUtils;
import com.example.utils.SystemConstants;
import com.example.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * 服务实现类
 *
 * @author xiaoning
 * @date 2022/10/08
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送验证码
     *
     * @param phone
     * @return
     */
    @Override
    public Result sendVerifyCode(String phone) {
        // 1、验证手机号是否合法
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2、手机号不合法，返回错误信息
            return Result.fail("手机号不合法！");
        }

        // 3、手机号合法，生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 4、将验证码存储到Redis中
        String redisKey = RedisConstants.LOGIN_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(redisKey, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // 5、发送验证码
        log.info("验证码已发送：{}，有效时间为5分钟，请尽快使用！", code);

        return Result.ok();
    }

    /**
     * 登录请求
     *
     * @param loginFormDTO
     * @return
     */
    @Override
    public Result login(LoginFormDTO loginFormDTO) {
        // 1、检查用户输入手机号是否合法
        String phone = loginFormDTO.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2、手机号不合法，返回出错误信息
            return Result.fail("手机号不合法！");
        }

        // 2、验证用户输入验证码是否正确
        String code = loginFormDTO.getCode();
        // 从Redis中获取正确验证码
        String realCode = (String) redisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_PREFIX + phone);
        if (realCode == null || !realCode.equals(code)) {
            // 3、验证码不正确，返回错误信息
            return Result.fail("验证码错误！");
        }

        // 4、判断用户是否注册
        User user = query().eq("phone", phone).one();

        if (user == null) {
            // 5、用户不存在，注册为用户，并保存到数据库
            user = registerUser(phone);
        }

        // 6、将用户信息保存到Redis中
        // 6.1、生成一个随机的token
        String token = UUID.randomUUID().toString(true);
        // 6.2、去除敏感信息
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 6.3、将对象转成Map
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO);
        // 6.4、存入Redis中
        String tokenKey = RedisConstants.LOGIN_TOKEN_PREFIX + token;
        redisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 6.5、设置token有效期
        redisTemplate.expire(RedisConstants.LOGIN_TOKEN_PREFIX + token, RedisConstants.TOKEN_TTL, TimeUnit.MINUTES);

        // 7、返回token
        return Result.ok(token);
    }

    /**
     * 获取个人信息
     *
     * @return
     */
    @Override
    public Result getProfile() {
        UserDTO userDTO = UserHolder.getUser();
        if (userDTO == null) {
            return Result.fail("请先登录！");
        }
        return Result.ok(UserHolder.getUser());
    }

    /**
     * 根据id查询用户信息
     *
     * @param id 用户id
     * @return
     */
    @Override
    public Result queryUserById(Long id) {
        User user = getById(id);
        if (user == null) {
            return Result.fail("用户信息不存在！");
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);

        return Result.ok(userDTO);
    }

    /**
     * 注册用户
     *
     * @param phone
     * @return
     */
    private User registerUser(String phone) {
        // 1、创建新用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.DEFAULT_NICK_NAME_PREFIX + RandomUtil.randomString(6));

        // 2、保存用户信息
        save(user);
        return user;
    }


}
