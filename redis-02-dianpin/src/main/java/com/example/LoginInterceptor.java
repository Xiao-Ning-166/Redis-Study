package com.example;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.example.dto.UserDTO;
import com.example.utils.RedisConstants;
import com.example.utils.UserHolder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 登录拦截器
 *
 * @author xiaoning
 * @date 2022/10/08
 */
public class LoginInterceptor implements HandlerInterceptor {

    private RedisTemplate redisTemplate;

    public LoginInterceptor(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 验证用户是否登录。登录则存入ThreadLocal中
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1、得到token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            // 未登录，拦截
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        // 2、从redis中获取用户信息
        String tokenKey = RedisConstants.LOGIN_TOKEN_PREFIX + token;
        Map<String, Object> userMap = redisTemplate.opsForHash().entries(tokenKey);

        // 3、判断用户是否存在
        if (userMap.isEmpty()) {
            // 用户不存在，拦截
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        // 4、将map转成UserDTO对象
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);

        // 5、刷新token在redis中的有效期
        redisTemplate.expire(tokenKey, RedisConstants.TOKEN_TTL, TimeUnit.MINUTES);

        // 6、将用户信息存储到ThreadLocal中
        UserHolder.saveUser(userDTO);

        // 7、放行
        return true;
    }

    /**
     * 清除ThreadLocal中用户信息
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除user信息
        UserHolder.removeUser();
    }
}
