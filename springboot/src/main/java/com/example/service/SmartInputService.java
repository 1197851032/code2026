package com.example.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能输入提示服务
 */
@Service
public class SmartInputService {

    // 预定义的快捷问题模板
    private static final List<Map<String, Object>> QUICK_TEMPLATES = Arrays.asList(
        Map.of(
            "category", "商品推荐",
            "templates", Arrays.asList(
                "推荐一些热门商品",
                "帮我推荐一些适合的商品",
                "有什么新商品推荐吗",
                "推荐性价比高的商品",
                "推荐一些特价商品"
            ),
            "keywords", Arrays.asList("推荐", "商品", "热门", "新品", "性价比", "特价")
        ),
        Map.of(
            "category", "价格查询",
            "templates", Arrays.asList(
                "这个商品多少钱",
                "查询商品价格",
                "有什么便宜的商品",
                "价格在100元以内的商品",
                "中等价位的商品推荐"
            ),
            "keywords", Arrays.asList("价格", "多少钱", "便宜", "贵", "预算", "价位")
        ),
        Map.of(
            "category", "商品分类",
            "templates", Arrays.asList(
                "有什么手机推荐",
                "推荐一些电脑",
                "服装类商品有哪些",
                "食品类商品推荐",
                "数码产品推荐"
            ),
            "keywords", Arrays.asList("手机", "电脑", "服装", "食品", "数码", "分类")
        ),
        Map.of(
            "category", "功能特性",
            "templates", Arrays.asList(
                "有什么拍照效果好的手机",
                "推荐性能强劲的电脑",
                "有什么耐用的商品",
                "推荐便携式设备",
                "有什么智能设备"
            ),
            "keywords", Arrays.asList("拍照", "性能", "耐用", "便携", "智能", "功能")
        ),
        Map.of(
            "category", "购买咨询",
            "templates", Arrays.asList(
                "如何购买商品",
                "购买流程是什么",
                "支持哪些支付方式",
                "配送需要多长时间",
                "可以退换货吗"
            ),
            "keywords", Arrays.asList("购买", "流程", "支付", "配送", "退换货", "售后")
        ),
        Map.of(
            "category", "库存查询",
            "templates", Arrays.asList(
                "这个商品有库存吗",
                "查询商品库存",
                "什么时候有货",
                "可以预订吗",
                "到货通知"
            ),
            "keywords", Arrays.asList("库存", "有货", "缺货", "预订", "到货", "通知")
        )
    );

    /**
     * 获取所有快捷提示模板
     */
    public List<Map<String, Object>> getAllTemplates() {
        return QUICK_TEMPLATES;
    }

    /**
     * 根据用户输入获取相关提示
     * @param userInput 用户输入内容
     * @return 相关提示列表
     */
    public List<Map<String, Object>> getRelevantTemplates(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            return QUICK_TEMPLATES.stream()
                    .limit(3)  // 默认显示前3个分类
                    .collect(Collectors.toList());
        }

        String input = userInput.toLowerCase().trim();
        List<Map<String, Object>> relevantTemplates = new ArrayList<>();

        // 计算每个分类的相关性得分
        for (Map<String, Object> category : QUICK_TEMPLATES) {
            double score = calculateRelevanceScore(input, category);
            if (score > 0) {
                Map<String, Object> categoryWithScore = new HashMap<>(category);
                categoryWithScore.put("relevanceScore", score);
                relevantTemplates.add(categoryWithScore);
            }
        }

        // 按相关性得分排序
        relevantTemplates.sort((a, b) -> 
            Double.compare((Double) b.get("relevanceScore"), (Double) a.get("relevanceScore")));

        // 如果没有匹配的，返回默认的几个分类
        if (relevantTemplates.isEmpty()) {
            return QUICK_TEMPLATES.stream()
                    .limit(3)
                    .collect(Collectors.toList());
        }

        return relevantTemplates.stream()
                .limit(5)  // 最多返回5个相关分类
                .collect(Collectors.toList());
    }

    /**
     * 获取特定分类的模板
     * @param category 分类名称
     * @return 模板列表
     */
    public List<String> getTemplatesByCategory(String category) {
        for (Map<String, Object> cat : QUICK_TEMPLATES) {
            if (category.equals(cat.get("category"))) {
                return (List<String>) cat.get("templates");
            }
        }
        return Collections.emptyList();
    }

    /**
     * 搜索包含关键词的模板
     * @param keyword 关键词
     * @return 匹配的模板列表
     */
    public List<Map<String, Object>> searchTemplates(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String kw = keyword.toLowerCase();
        List<Map<String, Object>> results = new ArrayList<>();

        for (Map<String, Object> category : QUICK_TEMPLATES) {
            List<String> templates = (List<String>) category.get("templates");
            List<String> matchingTemplates = templates.stream()
                    .filter(template -> template.toLowerCase().contains(kw))
                    .collect(Collectors.toList());

            if (!matchingTemplates.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("category", category.get("category"));
                result.put("matchingTemplates", matchingTemplates);
                result.put("matchCount", matchingTemplates.size());
                results.add(result);
            }
        }

        return results;
    }

    /**
     * 获取热门提示（使用频率高的）
     */
    public List<String> getPopularTemplates() {
        return Arrays.asList(
            "推荐一些热门商品",
            "这个商品多少钱",
            "有什么手机推荐",
            "如何购买商品",
            "这个商品有库存吗"
        );
    }

    /**
     * 计算用户输入与分类的相关性得分
     */
    private double calculateRelevanceScore(String input, Map<String, Object> category) {
        List<String> keywords = (List<String>) category.get("keywords");
        double score = 0;

        for (String keyword : keywords) {
            if (input.contains(keyword.toLowerCase())) {
                // 完全匹配关键词得分更高
                if (input.equals(keyword.toLowerCase())) {
                    score += 2.0;
                } else {
                    score += 1.0;
                }
            }
        }

        // 检查分类名称是否匹配
        String categoryName = ((String) category.get("category")).toLowerCase();
        if (input.contains(categoryName)) {
            score += 1.5;
        }

        return score;
    }

    /**
     * 获取智能建议
     * @param userInput 用户输入
     * @return 建议列表
     */
    public List<String> getSmartSuggestions(String userInput) {
        List<String> suggestions = new ArrayList<>();
        
        if (userInput == null || userInput.trim().isEmpty()) {
            return getPopularTemplates();
        }

        String input = userInput.toLowerCase().trim();

        // 基于输入长度和内容提供建议
        if (input.length() < 3) {
            // 输入很短，提供热门建议
            suggestions.addAll(getPopularTemplates().subList(0, 3));
        } else if (input.contains("推荐")) {
            suggestions.add("推荐一些热门商品");
            suggestions.add("推荐性价比高的商品");
            suggestions.add("推荐适合我的商品");
        } else if (input.contains("价格") || input.contains("多少钱")) {
            suggestions.add("这个商品多少钱");
            suggestions.add("有什么便宜的商品");
            suggestions.add("中等价位的商品推荐");
        } else if (input.contains("买") || input.contains("购买")) {
            suggestions.add("如何购买商品");
            suggestions.add("购买流程是什么");
            suggestions.add("支持哪些支付方式");
        } else if (input.contains("库存") || input.contains("有货")) {
            suggestions.add("这个商品有库存吗");
            suggestions.add("什么时候有货");
            suggestions.add("可以预订吗");
        }

        // 如果没有特定建议，返回相关模板
        if (suggestions.isEmpty()) {
            List<Map<String, Object>> relevant = getRelevantTemplates(input);
            for (Map<String, Object> category : relevant) {
                List<String> templates = (List<String>) category.get("templates");
                suggestions.add(templates.get(0));  // 取每个分类的第一个模板
                if (suggestions.size() >= 5) break;
            }
        }

        return suggestions.stream()
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }
}
