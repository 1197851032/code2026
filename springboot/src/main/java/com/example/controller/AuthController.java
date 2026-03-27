package com.example.controller;

import com.example.common.Result;
import com.example.controller.BaseController;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证相关接口
 */
@RestController
@RequestMapping("/auth")
public class AuthController extends BaseController {

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result getCurrentUser(HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", getCurrentUserId(request));
            userInfo.put("username", getCurrentUsername(request));
            userInfo.put("role", getCurrentUserRole(request));
            
            return Result.success(userInfo);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 验证token是否有效
     */
    @GetMapping("/validate")
    public Result validateToken(HttpServletRequest request) {
        try {
            // 如果能走到这里，说明token是有效的（因为拦截器已经验证过了）
            return Result.success("token有效");
        } catch (Exception e) {
            return Result.error("token无效");
        }
    }
}
