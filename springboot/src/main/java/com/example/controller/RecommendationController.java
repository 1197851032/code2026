package com.example.controller;

import com.example.common.Result;
import com.example.controller.BaseController;
import com.example.entity.Goods;
import com.example.service.RecommendationService;
import com.example.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 推荐系统控制器
 */
@RestController
@RequestMapping("/recommendation")
public class RecommendationController extends BaseController {

    @Autowired
    private RecommendationService recommendationService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取个性化推荐商品
     */
    @GetMapping("/goods")
    public Result getRecommendedGoods(HttpServletRequest request,
                                     @RequestParam(defaultValue = "10") int limit) {
        try {
            // 获取当前用户ID - 直接从JWT token获取
            Integer userId = null;
            try {
                String token = getTokenFromRequest(request);
                if (token != null) {
                    String username = jwtUtil.getUsernameFromToken(token);
                    if (jwtUtil.validateToken(token, username)) {
                        userId = jwtUtil.getUserIdFromToken(token);
                    }
                }
            } catch (Exception e) {
                // 用户未登录，返回热门商品
                List<Goods> popularGoods = recommendationService.getPopularGoods(limit);
                return Result.success(popularGoods);
            }
            
            if (userId == null) {
                List<Goods> popularGoods = recommendationService.getPopularGoods(limit);
                return Result.success(popularGoods);
            }
            
            // 获取个性化推荐
            List<Goods> recommendedGoods = recommendationService.recommendGoods(userId, limit);
            return Result.success(recommendedGoods);
            
        } catch (Exception e) {
            return Result.error("获取推荐商品失败：" + e.getMessage());
        }
    }
    
    /**
     * 从请求中获取JWT token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 获取热门商品（未登录用户或新用户）
     */
    @GetMapping("/popular")
    public Result getPopularGoods(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Goods> popularGoods = recommendationService.getPopularGoods(limit);
            return Result.success(popularGoods);
        } catch (Exception e) {
            return Result.error("获取热门商品失败：" + e.getMessage());
        }
    }

    /**
     * 获取管理员推荐商品
     */
    @GetMapping("/admin")
    public Result getAdminRecommendedGoods(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Goods> adminGoods = recommendationService.getAdminRecommendedGoods(limit);
            return Result.success(adminGoods);
        } catch (Exception e) {
            return Result.error("获取管理员推荐商品失败：" + e.getMessage());
        }
    }
}
