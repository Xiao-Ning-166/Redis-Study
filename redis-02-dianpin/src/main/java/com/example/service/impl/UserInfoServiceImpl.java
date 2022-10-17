package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.UserInfo;
import com.example.mapper.UserInfoMapper;
import com.example.service.IUserInfoService;
import org.springframework.stereotype.Service;

/**
 * @author xiaoning
 * @date 2022/10/17
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {
}
