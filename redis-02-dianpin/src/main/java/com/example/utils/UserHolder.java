package com.example.utils;

import com.example.dto.UserDTO;

/**
 * @author xiaoning
 * @date 2022/10/08
 */
public class UserHolder {
    private static final ThreadLocal<UserDTO> threadLocal = new ThreadLocal<>();

    public static void saveUser(UserDTO user){
        threadLocal.set(user);
    }

    public static UserDTO getUser(){
        return threadLocal.get();
    }

    public static void removeUser(){
        threadLocal.remove();
    }
}
