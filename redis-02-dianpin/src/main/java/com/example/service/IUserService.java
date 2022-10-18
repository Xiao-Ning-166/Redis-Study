package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dto.LoginFormDTO;
import com.example.dto.Result;
import com.example.entity.User;

import javax.servlet.http.HttpSession;

/**
 * @author xiaoning
 * @date 2022/10/08
 */
public interface IUserService extends IService<User> {

    /**
     * 发送验证码
     *
     * @param phone
     * @return
     */
    Result sendVerifyCode(String phone);

    /**
     * 登录请求
     *
     * @param loginFormDTO
     * @return
     */
    Result login(LoginFormDTO loginFormDTO);

    /**
     * 获取个人信息
     *
     * @return
     */
    Result getProfile();

    /**
     * 根据id查询用户信息
     *
     * @param id 用户id
     * @return
     */
    Result queryUserById(Long id);
}
