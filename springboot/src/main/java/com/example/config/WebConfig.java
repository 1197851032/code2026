package com.example.config;

import com.example.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Value("${fileBaseUrl}")
    private String fileBaseUrl;

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
                        "/files/upload",   // 文件上传接口
                        "/files/**",       // 文件下载和静态资源访问（不需要登录）
                        "/goods/**",       // 商品相关接口（允许访问）
                        "/category/**"     // 分类相关接口（允许访问）
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

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置文件上传的静态资源访问路径
        String filePath = System.getProperty("user.dir") + "/files/";
        File uploadDir = new File(filePath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + filePath);
    }
}
