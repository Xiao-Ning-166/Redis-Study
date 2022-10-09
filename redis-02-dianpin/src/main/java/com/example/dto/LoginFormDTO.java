package com.example.dto;

import lombok.Data;

/**
 * 登录表单
 *
 * @author xiaoning
 * @date 2022/10/08
 */
@Data
public class LoginFormDTO {

    /**
     * 手机号
     */
    private String phone;

    /**
     * 验证码
     */
    private String code;

    /**
     * 密码
     */
    private String password;

}
