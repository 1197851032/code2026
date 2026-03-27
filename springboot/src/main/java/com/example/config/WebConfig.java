package com.example.config;

import com.example.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns(
                        "/login",          // 登录接口
                        "/register",       // 注册接口
                        "/user/login",     // 登录接口（完整路径）
                        "/user/register",  // 注册接口（完整路径）
                        "/ai/simple-chat", // 简单聊天接口（不需要登录）
                        "/ai/chat",        // RAG聊天接口（不需要登录）
                        "/goods/**",       // 商品相关接口（不需要登录）
                        "/category/**",    // 分类相关接口（不需要登录）
                        "/carousel/**",    // 轮播图接口（不需要登录）
                        "/files/**",       // 文件访问接口（不需要登录）
                        "/file/**",        // 文件上传接口（不需要登录）
                        "/admin/**",       // 管理员接口（不需要登录）
                        "/recommendation/**", // 推荐系统接口（不需要登录）
                        "/error",          // 错误页面
                        "/favicon.ico"     // 图标
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
