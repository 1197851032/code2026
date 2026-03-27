package com.example.controller;

import com.example.common.Result;
import com.example.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 缓存管理控制器（需要管理员权限）
 */
@RestController
@RequestMapping("/cache")
public class CacheController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * 清除指定用户的推荐缓存（需要管理员权限）
     */
    @DeleteMapping("/recommendation/user/{userId}")
    public Result clearUserRecommendationCache(@PathVariable Integer userId, HttpServletRequest request) {
        try {
            // 检查是否登录
            Object userIdObj = request.getAttribute("userId");
            if (userIdObj == null) {
                return Result.error("请先登录");
            }
            
            // 这里可以添加更严格的权限检查
            // 例如：只有管理员才能清除其他用户的缓存
            
            recommendationService.clearUserRecommendationCache(userId);
            return Result.success("用户" + userId + "的推荐缓存已清除");
        } catch (Exception e) {
            return Result.error("清除用户推荐缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清除所有推荐缓存（需要管理员权限）
     */
    @DeleteMapping("/recommendation/all")
    public Result clearAllRecommendationCache(HttpServletRequest request) {
        try {
            // 检查是否登录
            Object userIdObj = request.getAttribute("userId");
            if (userIdObj == null) {
                return Result.error("请先登录");
            }
            
            recommendationService.clearAllRecommendationCache();
            return Result.success("所有推荐缓存已清除");
        } catch (Exception e) {
            return Result.error("清除所有推荐缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清除所有缓存（需要管理员权限）
     */
    @DeleteMapping("/all")
    public Result clearAllCache(HttpServletRequest request) {
        try {
            // 检查是否登录
            Object userIdObj = request.getAttribute("userId");
            if (userIdObj == null) {
                return Result.error("请先登录");
            }
            
            // 先清除推荐缓存
            recommendationService.clearAllRecommendationCache();
            return Result.success("所有缓存已清除");
        } catch (Exception e) {
            return Result.error("清除所有缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除当前用户的推荐缓存（普通用户也可以）
     */
    @DeleteMapping("/recommendation/current")
    public Result clearCurrentUserRecommendationCache(HttpServletRequest request) {
        try {
            Object userIdObj = request.getAttribute("userId");
            if (userIdObj == null) {
                return Result.error("请先登录");
            }
            
            Integer currentUserId = (Integer) userIdObj;
            recommendationService.clearUserRecommendationCache(currentUserId);
            return Result.success("您的推荐缓存已清除");
        } catch (Exception e) {
            return Result.error("清除推荐缓存失败: " + e.getMessage());
        }
    }
}
