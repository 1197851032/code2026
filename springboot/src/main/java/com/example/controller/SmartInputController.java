package com.example.controller;

import com.example.common.Result;
import com.example.service.SmartInputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 智能输入提示控制器
 */
@RestController
@RequestMapping("/smart-input")
@CrossOrigin(origins = "*")
public class SmartInputController {

    @Autowired
    private SmartInputService smartInputService;

    /**
     * 获取所有快捷提示模板
     */
    @GetMapping("/templates")
    public Result getAllTemplates() {
        try {
            List<Map<String, Object>> templates = smartInputService.getAllTemplates();
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error("获取提示模板失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户输入获取相关提示
     */
    @GetMapping("/relevant")
    public Result getRelevantTemplates(@RequestParam String input) {
        try {
            List<Map<String, Object>> relevant = smartInputService.getRelevantTemplates(input);
            return Result.success(relevant);
        } catch (Exception e) {
            return Result.error("获取相关提示失败: " + e.getMessage());
        }
    }

    /**
     * 获取特定分类的模板
     */
    @GetMapping("/category/{category}")
    public Result getTemplatesByCategory(@PathVariable String category) {
        try {
            List<String> templates = smartInputService.getTemplatesByCategory(category);
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error("获取分类模板失败: " + e.getMessage());
        }
    }

    /**
     * 搜索包含关键词的模板
     */
    @GetMapping("/search")
    public Result searchTemplates(@RequestParam String keyword) {
        try {
            List<Map<String, Object>> results = smartInputService.searchTemplates(keyword);
            return Result.success(results);
        } catch (Exception e) {
            return Result.error("搜索模板失败: " + e.getMessage());
        }
    }

    /**
     * 获取热门提示
     */
    @GetMapping("/popular")
    public Result getPopularTemplates() {
        try {
            List<String> popular = smartInputService.getPopularTemplates();
            return Result.success(popular);
        } catch (Exception e) {
            return Result.error("获取热门提示失败: " + e.getMessage());
        }
    }

    /**
     * 获取智能建议
     */
    @GetMapping("/suggestions")
    public Result getSmartSuggestions(@RequestParam(required = false) String input) {
        try {
            List<String> suggestions = smartInputService.getSmartSuggestions(input);
            return Result.success(suggestions);
        } catch (Exception e) {
            return Result.error("获取智能建议失败: " + e.getMessage());
        }
    }

    /**
     * 获取智能输入配置信息
     */
    @GetMapping("/config")
    public Result getSmartInputConfig() {
        try {
            Map<String, Object> config = Map.of(
                "enableSmartInput", true,
                "maxSuggestions", 5,
                "showPopularOnEmpty", true,
                "categories", smartInputService.getAllTemplates().stream()
                        .map(template -> template.get("category"))
                        .toList(),
                "features", Map.of(
                    "autoComplete", true,
                    "categoryFilter", true,
                    "keywordSearch", true,
                    "smartRanking", true
                )
            );
            return Result.success(config);
        } catch (Exception e) {
            return Result.error("获取配置信息失败: " + e.getMessage());
        }
    }
}
