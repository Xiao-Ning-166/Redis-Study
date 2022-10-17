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
                // 排除所有
                .excludePathPatterns("/**")
                // 设置拦截的路径
                .addPathPatterns("/user/info/**", "/user/me",
                        "/blog", "/blog/like/**", "/blog/of/**",
                        "/upload/**",
                        "/voucher/seckill",
                        "/voucher-order/**");
    }
}
