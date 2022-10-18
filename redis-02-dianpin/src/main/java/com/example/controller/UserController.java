package com.example.controller;

import com.example.dto.LoginFormDTO;
import com.example.dto.Result;
import com.example.entity.UserInfo;
import com.example.service.IUserInfoService;
import com.example.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * @author xiaoning
 * @date 2022/10/08
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * 获取手机验证码
     *
     * @param phone 手机号
     */
    @PostMapping("/code")
    public Result getVerifyCode(String phone, HttpSession session) {

        return userService.sendVerifyCode(phone);
    }


    /**
     * 登录请求
     *
     * @param loginFormDTO
     * @return
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginFormDTO) {
       return userService.login(loginFormDTO);
    }

    /**
     * 获取个人信息
     *
     * @return
     */
    @GetMapping("/me")
    public Result me(){
        return userService.getProfile();
    }

    /**
     * 根据id查询用户信息
     *
     * @param id 用户id
     * @return
     */
    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable("id") Long id) {
        return userService.queryUserById(id);
    }


    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId){
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }

}
