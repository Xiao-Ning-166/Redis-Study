package com.example.config;

import com.example.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc配置类
 *
 * @author xiaoning
 * @date 2022/10/08
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor(redisTemplate))
                // 排除的路径
                .excludePathPatterns("/user/code", "/user/login",
                        "/shop/**", "/shop-type/**",
                        "/voucher/list/**");
    }
}
