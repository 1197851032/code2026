package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable()) // 禁用Spring Security的CORS，让WebConfig处理
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // 允许所有请求
            )
            .headers(headers -> headers.frameOptions().disable()); // 禁用frame options
        
        return http.build();
    }
}
