package com.example.controller;

import jakarta.servlet.http.HttpServletRequest;

public class BaseController {

    /**
     * 获取当前登录用户ID
     */
    protected Integer getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        return (Integer) userId;
    }

    /**
     * 获取当前登录用户名
     */
    protected String getCurrentUsername(HttpServletRequest request) {
        Object username = request.getAttribute("username");
        if (username == null) {
            throw new RuntimeException("用户未登录");
        }
        return (String) username;
    }

    /**
     * 获取当前登录用户角色
     */
    protected String getCurrentUserRole(HttpServletRequest request) {
        Object role = request.getAttribute("role");
        if (role == null) {
            throw new RuntimeException("用户未登录");
        }
        return (String) role;
    }

    /**
     * 检查是否为管理员
     */
    protected boolean isAdmin(HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        return "ADMIN".equals(role) || "管理员".equals(role);
    }

    /**
     * 检查是否为普通用户
     */
    protected boolean isUser(HttpServletRequest request) {
        return "USER".equals(getCurrentUserRole(request));
    }
}
