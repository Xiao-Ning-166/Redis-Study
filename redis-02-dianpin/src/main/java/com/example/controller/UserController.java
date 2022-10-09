package com.example.controller;

import com.example.dto.LoginFormDTO;
import com.example.dto.Result;
import com.example.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
